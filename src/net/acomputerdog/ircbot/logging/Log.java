package net.acomputerdog.ircbot.logging;

import net.acomputerdog.core.logger.CLogger;

import java.io.*;

public class Log {
    private final LogManager manager;
    private final String name;
    private final CLogger logger;
    private final File logFile;
    private final int logNum;
    private final OutputStream fileOut;

    public Log(LogManager manager, String name) throws FileNotFoundException {
        if (name == null) throw new IllegalArgumentException("Name cannot be null!");
        if (manager == null) throw new IllegalArgumentException("Manager cannot be null!");
        this.manager = manager;
        this.name = name;
        this.logNum = findAvailableNum(manager.getLogDir(), name);
        this.logFile = new File(manager.getLogDir(), "/" + logNum + "/" + name + ".log");
        this.logger = new CLogger(name, false, true);
        fileOut = new BufferedOutputStream(new FileOutputStream(logFile));
        if ("Global".equals(name)) {
            logger.setLoggerOutput(new PrintStream(new SplittingOutputStream(fileOut, System.out)));
        } else {
            logger.setLoggerOutput(new PrintStream(new SplittingOutputStream(fileOut, new SplittingOutputStream(System.out, manager.getGlobalLog().getFileOut()))));
        }
        manager.addStream(fileOut);
    }

    public LogManager getManager() {
        return manager;
    }

    public String getName() {
        return name;
    }

    public CLogger getLogger() {
        return logger;
    }

    public File getLogFile() {
        return logFile;
    }

    public int getLogNum() {
        return logNum;
    }

    OutputStream getFileOut() {
        return fileOut;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Log)) return false;

        Log log = (Log) o;
        return name.equals(log.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    private static int findAvailableNum(File dir, String name) {
        //todo replace with java.nio
        int num = -1; //will start at 0 because num++ is at start of loop
        File currDir;
        File currFile;
        do {
            num++;
            currDir = new File(dir, "/" + num + "/");
            currFile = new File(currDir, "/" + name + ".log");
        } while (currFile.isFile());
        currDir.mkdirs();
        return num;
    }
}
