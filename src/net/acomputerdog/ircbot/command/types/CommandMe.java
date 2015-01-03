package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandMe extends Command {
    public CommandMe() {
        super("Me", "me", "emote", "action");
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        target.sendAction(command.args);
        return true;
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "me <action>";
    }
}
