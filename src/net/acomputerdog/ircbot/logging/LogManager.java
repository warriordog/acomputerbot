package net.acomputerdog.ircbot.logging;

import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.main.IrcBot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LogManager {

    private final Map<String, Log> loggerMap = new HashMap<>();
    private final List<OutputStream> openStreams = new LinkedList<>();
    private final IrcBot irc;
    private final File logDir;
    private final Log globalLog;

    private CLogger LOGGER;

    public LogManager(IrcBot irc) throws FileNotFoundException {
        LOGGER = new CLogger("LogManager-UNLOGGED", false, true);
        this.irc = irc;
        logDir = new File("./logs/");
        if (!logDir.isDirectory() && !logDir.mkdirs()) {
            LOGGER.logError("Could not initialize logging directory!");
        }
        globalLog = getLog("Global");
        LOGGER = getLogger("LogManager");
    }

    public Log getLog(String name) throws FileNotFoundException {
        Log log = loggerMap.get(name);
        if (log == null) {
            loggerMap.put(name, log = new Log(this, name));
        }
        return log;
    }

    public CLogger getLogger(String name) {
        try {
            return getLog(name).getLogger();
        } catch (FileNotFoundException e) {
            LOGGER.logError("Exception creating logger \"" + name + "\"!", e);
        }
        return new CLogger(name + "-UNLOGGED", false, true);
    }

    public File getLogDir() {
        return logDir;
    }

    public Log getGlobalLog() {
        return globalLog;
    }

    void addStream(OutputStream out) {
        if (out != null) {
            openStreams.add(out);
        }
    }

    public void onShutdown() {
        for (OutputStream stream : openStreams) {
            try {
                stream.close();
            } catch (Exception ignored) {}
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogManager)) return false;

        LogManager that = (LogManager) o;

        if (!loggerMap.equals(that.loggerMap)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return loggerMap.hashCode();
    }
}
