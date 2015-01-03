package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandLogout extends Command {
    public CommandLogout(IrcBot bot) {
        super(bot, "Logout", "logout", "deauth", "de-auth", "de_auth");
    }

    @Override
    public boolean allowedInChannel(Channel channel, User user) {
        return false;
    }

    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        if (bot.getAuth().deauthenticate(sender)) {
            sender.send("You have been successfully logged out!");
            return true;
        } else {
            sender.send(colorError("You are not logged in!"));
            return false;
        }
    }
}
