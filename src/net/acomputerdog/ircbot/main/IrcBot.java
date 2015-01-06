package net.acomputerdog.ircbot.main;

import com.sorcix.sirc.IrcConnection;
import com.sorcix.sirc.NickNameException;
import net.acomputerdog.core.java.MemBuffer;
import net.acomputerdog.core.java.Sleep;
import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.config.Admins;
import net.acomputerdog.ircbot.config.AutoJoinList;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.irc.IrcListener;
import net.acomputerdog.ircbot.logging.LogManager;
import net.acomputerdog.ircbot.security.Auth;
import net.acomputerdog.ircbot.security.BlackList;
import net.acomputerdog.ircbot.security.NickServ;
import net.acomputerdog.ircbot.security.StringCheck;

public class IrcBot {
    public static final IrcBot instance = new IrcBot();
    public static CLogger LOGGER;

    private final MemBuffer buffer = new MemBuffer();

    private boolean isRunning = false;
    private boolean canRun = true;
    private String shutdownReason = null;
    private boolean properShutdown = false;

    private IrcListener handler;
    private IrcConnection connection;
    private NickServ nickServ;
    private Admins admins;
    private Auth auth;
    private StringCheck stringCheck;
    private BlackList blacklist;
    private AutoJoinList autoJoinList;
    private LogManager logManager;

    private IrcBot() {
        if (instance != null) {
            throw new IllegalStateException("Cannot create more than one IrcBot!");
        }
    }

    private void start() {
        if (!isRunning) {
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
        } else {
            throw new IllegalArgumentException("Cannot start more than one IrcBot!");
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

        admins = new Admins(this);
        auth = new Auth(this);
        blacklist = new BlackList(this);
        stringCheck = new StringCheck(this);
        autoJoinList = new AutoJoinList(this);
        admins.load();
        blacklist.load();
        autoJoinList.load();

        handler = new IrcListener(this);
        Command.init(this);
        LOGGER.logInfo("Loaded " + Command.getCommandNameMap().size() + " commands with " + Command.getCommandMap().size() + " aliases.");

        IrcConnection.ABOUT_ADDITIONAL += (" + " + getVersionString());
        connection = new IrcConnection(Config.SERVER);
        if (Config.USE_LOGIN) {
            connection.setUsername(Config.BOT_USERNAME);
        }
        connection.setNick(Config.BOT_NICK);
        connection.addMessageListener(handler);
        connection.addServerListener(handler);
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
                connection.disconnect(shutdownReason == null ? "Bot shutting down." : shutdownReason);
            }
            blacklist.save();
            autoJoinList.save();
            admins.save();
            Config.save();
            IrcConnection.ABOUT_ADDITIONAL = "";
            logManager.onShutdown();
            properShutdown = true;
        } catch (Throwable ignored) {}
        System.exit(code);
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

    public IrcListener getHandler() {
        return handler;
    }

    public IrcConnection getConnection() {
        return connection;
    }

    public String getVersionString() {
        return "AcomputerBot v0.14";
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

    public static void main(String[] args) {
	    instance.start();
    }
}
