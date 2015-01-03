package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.IrcColors;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandInfo extends Command {
    public CommandInfo(IrcBot bot) {
        super(bot, "Info", "info", "inf", "about");
    }

    private String colorYellow(String message) {
        return IrcColors.color(message, IrcColors.YELLOW);
    }

    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        target.send(colorYellow("This is a simple bot made by acomputerdog.  I am open source and written in java!  Type \"" + Config.COMMAND_PREFIX + "help\" for commands."));
        return true;
    }
}
