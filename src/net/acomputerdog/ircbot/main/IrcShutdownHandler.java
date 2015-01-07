package net.acomputerdog.ircbot.main;

import com.sorcix.sirc.IrcConnection;
import net.acomputerdog.core.java.MemBuffer;
import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.logging.LogManager;

public class IrcShutdownHandler extends Thread {
    private final MemBuffer runBuffer = new MemBuffer();
    private final MemBuffer exceptionBuffer = new MemBuffer();
    private final CLogger LOGGER;
    private final IrcBot bot;

    public IrcShutdownHandler(IrcBot bot) {
        super();
        this.bot = bot;
        LOGGER = bot.getLogManager().getLogger("ShutdownHandler");
        super.setName("ShutdownHandler");
        runBuffer.allocate(1000);
        exceptionBuffer.allocate(1000);
        super.setDaemon(false);
        LOGGER.logInfo("ShutDown handler loaded.");
    }

    @Override
    public void run() {
        try {
            runBuffer.deallocate();
            if (!bot.isProperShutdown()) {
                LOGGER.logWarning("Detected unexpected shutdown!");
                IrcConnection connection = bot.getConnection();
                if (connection != null) {
                    connection.disconnect("Unexpected shutdown (probably crashed)");
                }
                LogManager manager = bot.getLogManager();
                if (manager != null) {
                    manager.onShutdown();
                }
            }
        } catch (Throwable t) {
            exceptionBuffer.free();
            LOGGER.logError("Exception in shutdown handler!", t);
        }
    }
}
