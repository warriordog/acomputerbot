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
        target.send(colorYellow("I am running " + bot.getVersionString() + "!  AcomputerBot is a free, open source IRC bot written by acomputerdog!  Type \"" + Config.COMMAND_PREFIX + "help\" for a list of commands."));
        return true;
    }
}
