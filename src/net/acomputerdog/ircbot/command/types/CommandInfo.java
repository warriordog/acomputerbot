package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandInfo extends Command {
    public CommandInfo(IrcBot bot) {
        super(bot, "Info", "info", "inf", "about");
    }

    @Override
    public String getDescription() {
        return "Prints information about AcomputerBot.";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        target.send(colorYellow("This is a simple bot made by acomputerdog.  I am open source and written in java!  Type \"" + Config.COMMAND_PREFIX + "help\" for commands."));
        return true;
    }
}
