package net.acomputerdog.ircbot.plugin;

import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.main.IrcBot;

public interface IrcPlugin {

    public default CLogger getLogger() {
        return getIrcBot().getLogManager().getLogger(getID());
    }

    public IrcBot getIrcBot();

    public String getID();

    public default String getName() {
        return getID();
    }

    public void onLoad(IrcBot bot);

    public void onUnload();

}
