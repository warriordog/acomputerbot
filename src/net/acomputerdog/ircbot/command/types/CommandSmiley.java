package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandSmiley extends Command {
    public CommandSmiley(IrcBot bot) {
        super(bot, "Smiley", "smiley", "smile");
    }

    @Override
    public String getDescription() {
        return "Prints a smiley face.";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        target.send("☺");
        return true;
    }
}
