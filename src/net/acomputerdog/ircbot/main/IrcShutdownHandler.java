package net.acomputerdog.ircbot.main;

import com.sorcix.sirc.IrcConnection;
import net.acomputerdog.core.java.MemBuffer;
import net.acomputerdog.core.logger.CLogger;

public class IrcShutdownHandler extends Thread {
    private final MemBuffer runBuffer = new MemBuffer();
    private final MemBuffer exceptionBuffer = new MemBuffer();
    private final CLogger LOGGER;
    private final IrcBot bot;

    public IrcShutdownHandler(IrcBot bot) {
        super();
        this.bot = bot;
        LOGGER = bot.getLogManager().getLogger("ShutdownHandler");
        super.setName("IrcBot_Shutdown_Handler");
        runBuffer.allocate(1000);
        exceptionBuffer.allocate(1000);
    }

    @Override
    public void run() {
        try {
            runBuffer.deallocate();
            if (bot.canRun()) {
                LOGGER.logWarning("Detected unexpected shutdown!");
                IrcConnection connection = bot.getConnection();
                if (connection != null) {
                    connection.disconnect("Unexpected shutdown (probably crashed)");
                }
            }
        } catch (Throwable t) {
            exceptionBuffer.free();
            LOGGER.logError("Exception in shutdown handler!", t);
        }
    }
}
