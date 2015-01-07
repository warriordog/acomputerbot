package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandGithub extends Command {
    public CommandGithub(IrcBot bot) {
        super(bot, "Github", "github", "source", "src");
    }

    @Override
    public String getDescription() {
        return "Prints a link to AcomputerBot's github repository.";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        target.send("My source code can be found on github here: https://github.com/warriordog/acomputerbot");
        return true;
    }
}
