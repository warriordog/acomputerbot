package net.acomputerdog.ircbot.main;

import com.sorcix.sirc.IrcConnection;
import net.acomputerdog.core.java.MemBuffer;
import net.acomputerdog.core.java.Sleep;
import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.config.Admins;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.irc.IrcListener;
import net.acomputerdog.ircbot.security.Auth;
import net.acomputerdog.ircbot.security.NickservListener;

public class IrcBot {
    public static final IrcBot instance = new IrcBot();
    public static CLogger LOGGER;

    private final MemBuffer buffer = new MemBuffer();

    private boolean isRunning = false;
    private boolean canRun = true;

    private IrcListener handler;
    private IrcConnection connection;
    private NickservListener nickservListener;

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
        Config.load();
        LOGGER = new CLogger(Config.BOT_USERNAME, false, true);
        buffer.allocate(Config.MEMORY_BUFFER_SIZE);
        LOGGER.logInfo("Beginning startup.");
        Runtime.getRuntime().addShutdownHook(new IrcShutdownHandler(this));

        Admins.load();

        handler = new IrcListener(this);
        Command.init();
        LOGGER.logInfo("Loaded " + Command.getCommandNameMap().size() + " commands with " + Command.getCommandMap().size() + " aliases.");

        connection = new IrcConnection(Config.SERVER);
        if (Config.USE_LOGIN) {
            connection.setUsername(Config.BOT_USERNAME);
        }
        connection.setNick(Config.BOT_NICK);
        connection.addMessageListener(handler);
        connection.addServerListener(handler);
        connection.addMessageListener(nickservListener = new NickservListener(this));
        try {
            LOGGER.logInfo("Connecting to " + Config.SERVER + "...");
            connection.connect();
            LOGGER.logInfo("Connected.");
        } catch (Exception e) {
            LOGGER.logFatal("Unable to connect to IRC network!", e);
            end(-1);
        }
        if (Config.USE_LOGIN) {
            nickservListener.getNickServ().send("GHOST " + Config.BOT_USERNAME + " " + Config.BOT_PASS);
            nickservListener.getNickServ().send("IDENTIFY " + Config.BOT_PASS);
        }

        LOGGER.logInfo("Startup complete.");
    }

    private void onTick() {
        Auth.tick();
    }

    private void end(int code) {
        if (code == 0) {
            LOGGER.logInfo("Shutting down normally.");
        } else {
            LOGGER.logWarning("Shutting down unexpectedly with code " + code + "!");
        }
        try {
            if (connection != null) {
                connection.disconnect("Bot shutting down.");
            }
            Admins.save();
            Config.save();
        } catch (Throwable ignored) {}
        System.exit(code);
    }

    public void stop() {
        canRun = false;
    }

    public IrcListener getHandler() {
        return handler;
    }

    public IrcConnection getConnection() {
        return connection;
    }

    public String getVersionString() {
        return "AcomputerBot v0.3";
    }

    public boolean canRun() {
        return canRun;
    }

    public NickservListener getNickservListener() {
        return nickservListener;
    }

    public static void main(String[] args) {
	    instance.start();
    }
}
