package net.acomputerdog.ircbot.main;

import com.sorcix.sirc.main.IrcConnection;
import com.sorcix.sirc.util.NickNameException;
import net.acomputerdog.core.java.MemBuffer;
import net.acomputerdog.core.java.Sleep;
import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.command.CommandManager;
import net.acomputerdog.ircbot.config.Admins;
import net.acomputerdog.ircbot.config.AutoJoinList;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.irc.IrcListener;
import net.acomputerdog.ircbot.logging.LogManager;
import net.acomputerdog.ircbot.plugin.IrcPlugin;
import net.acomputerdog.ircbot.plugin.PluginList;
import net.acomputerdog.ircbot.plugin.PluginLoader;
import net.acomputerdog.ircbot.security.Auth;
import net.acomputerdog.ircbot.security.BlackList;
import net.acomputerdog.ircbot.security.NickServ;
import net.acomputerdog.ircbot.security.StringCheck;

import java.io.File;

public class IrcBot {
    @Deprecated
    public static IrcBot instance = new IrcBot();
    public CLogger LOGGER;

    private final MemBuffer buffer = new MemBuffer();

    private boolean isRunning = false;
    private boolean canRun = true;
    private String shutdownReason = null;
    private boolean properShutdown = false;
    private boolean reloading = false;

    private IrcListener handler;
    private IrcConnection connection;
    private NickServ nickServ;
    private Admins admins;
    private Auth auth;
    private StringCheck stringCheck;
    private BlackList blacklist;
    private AutoJoinList autoJoinList;
    private LogManager logManager;
    private CommandManager commandManager;
    private PluginLoader pluginLoader;
    private PluginList pluginList;

    private IrcBot() {
        instance = this;
    }

    public static void main(String[] args) {
        while (true) {
            //loops forever to support reloads
            new IrcBot().start();
            System.setSecurityManager(null); //disable JS sandbox
            instance = null;
            Sleep.sleep(1000);
            System.gc();
        }
    }

    private void start() {
        isRunning = true;
        try {
            init();
            while (canRun) {
                long methodStartTime = System.currentTimeMillis();
                onTick();
                Sleep.sync(methodStartTime, 1000 / Config.TPS);
            }
            end(0);
        } catch (Throwable t) {
            buffer.free();
            LOGGER.logFatal("Uncaught exception in IrcBot loop!", t);
            end(-1);
        }
    }

    private void init() {
        try {
            logManager = new LogManager(this);
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize log manager!", e);
        }
        Config.load();
        LOGGER = logManager.getLogger(Config.BOT_USERNAME);
        buffer.allocate(Config.MEMORY_BUFFER_SIZE);
        LOGGER.logInfo(getVersionString() + " starting.");
        Runtime.getRuntime().addShutdownHook(new IrcShutdownHandler(this));

        pluginLoader = new PluginLoader();
        pluginList = pluginLoader.getPlugins();
        File pluginDir = new File("./plugins/");
        if (!pluginDir.isDirectory()) {
            pluginDir.mkdir();
        } else {
            try {
                pluginLoader.loadPlugins(new File("./plugins/"));
            } catch (Exception e) {
                LOGGER.logWarning("Unable to load plugins, e");
            }
        }

        admins = new Admins(this);
        auth = new Auth(this);
        blacklist = new BlackList(this);
        stringCheck = new StringCheck(this);
        autoJoinList = new AutoJoinList(this);
        admins.load();
        blacklist.load();
        autoJoinList.load();

        handler = new IrcListener(this);
        commandManager = new CommandManager(this);

        for (IrcPlugin plugin : pluginList.getPlugins()) {
            try {
                plugin.onLoad(this);
            } catch (Throwable t) {
                LOGGER.logError("Exception loading plugin " + plugin.getName() + "!", t);
            }
        }
        LOGGER.logInfo("Loaded " + pluginList.size() + " plugins.");

        LOGGER.logInfo("Loaded " + commandManager.getCommandNameMap().size() + " commands with " + commandManager.getCommandMap().size() + " aliases.");

        IrcConnection.ABOUT_ADDITIONAL += (getVersionString());
        connection = new IrcConnection(Config.SERVER);
        if (Config.USE_LOGIN) {
            connection.setUsername(Config.BOT_USERNAME);
        }
        connection.setNick(Config.BOT_NICK);
        connection.addMessageListener(handler);
        connection.addServerListener(handler);
        connection.setUnknownListener(handler);
        connection.addMessageListener(nickServ = new NickServ(this));
        connection.setMessageDelay(Config.MESSAGES_PER_SECOND);
        connection.setVersion(getVersionString());
        try {
            LOGGER.logInfo("Connecting to " + Config.SERVER + "...");
            connection.connect();
            LOGGER.logInfo("Connected.");
        } catch (NickNameException e) {
            LOGGER.logFatal("Nickname " + Config.BOT_NICK + " is already in use!");
            end(-2);
        } catch (Exception e) {
            LOGGER.logFatal("Unable to connect to IRC network!", e);
            end(-1);
        }
        if (Config.USE_LOGIN) {
            nickServ.send("GHOST " + Config.BOT_USERNAME + " " + Config.BOT_PASS);
            nickServ.send("IDENTIFY " + Config.BOT_PASS);
        }

        LOGGER.logInfo("Startup complete.");
        autoJoinList.joinChannels();
    }

    private void onTick() {
        auth.tick();
    }

    private void end(int code) {
        buffer.free();
        waitForCommandComplete();
        if (code == 0) {
            LOGGER.logInfo("Shutting down normally.");
        } else {
            LOGGER.logWarning("Shutting down unexpectedly with code " + code + "!");
        }
        if (shutdownReason != null) {
            LOGGER.logInfo("Shutdown reason: " + shutdownReason);
        }
        try {
            if (connection != null) {
                if (reloading) {
                    connection.disconnect("Bot reloading.");
                } else {
                    connection.disconnect(shutdownReason == null ? "Bot shutting down." : shutdownReason);
                }
            }

            for (IrcPlugin plugin : pluginList.getPlugins()) {
                try {
                    plugin.onUnload();
                } catch (Throwable t) {
                    LOGGER.logError("Exception unloading plugin " + plugin.getName() + "!", t);
                }
            }
            LOGGER.logInfo("Unloaded " + pluginList.size() + " plugins.");

            blacklist.save();
            autoJoinList.save();
            admins.save();
            Config.save();
            IrcConnection.ABOUT_ADDITIONAL = "";
            logManager.onShutdown();
            properShutdown = true;
        } catch (Throwable ignored) {}
        if (!reloading) {
            System.exit(code);
        }
    }

    private void waitForCommandComplete() {
        long startTime = System.currentTimeMillis();
        while (commandManager.isCommandInProgress() && System.currentTimeMillis() - startTime < 10000) {
            Sleep.sleep(100);
        }
    }

    public boolean isProperShutdown() {
        return properShutdown;
    }

    public void stop() {
        canRun = false;
    }

    public void stop(String reason) {
        shutdownReason = reason;
        stop();
    }

    public void reload() {
        reloading = true;
        stop();
    }

    public IrcListener getHandler() {
        return handler;
    }

    public IrcConnection getConnection() {
        return connection;
    }

    public String getVersionString() {
        return "AcomputerBot v0.15";
    }

    public boolean canRun() {
        return canRun;
    }

    public NickServ getNickServ() {
        return nickServ;
    }

    public Admins getAdmins() {
        return admins;
    }

    public Auth getAuth() {
        return auth;
    }

    public StringCheck getStringCheck() {
        return stringCheck;
    }

    public BlackList getBlacklist() {
        return blacklist;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public PluginLoader getPluginLoader() {
        return pluginLoader;
    }

    public PluginList getPluginList() {
        return pluginList;
    }
}
