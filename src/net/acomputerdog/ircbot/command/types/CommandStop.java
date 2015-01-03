package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Admins;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandStop extends Command {
    public CommandStop() {
        super("Stop", "stop", "shutdown");
    }

    @Override
    public boolean allowedInChannel(Channel channel, User user) {
        return Admins.isAdmin(user);
    }

    @Override
    public boolean allowedInPM(User user) {
        return Admins.isAdmin(user);
    }

    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        target.send("Bye bye!");
        bot.stop();
        return true;
    }
}
