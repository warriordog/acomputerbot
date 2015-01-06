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
    public String getDescription() {
        return "Log out as admin.";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        if (bot.getAuth().deauthenticate(sender)) {
            target.send("You have been successfully logged out!");
            return true;
        } else {
            target.send(colorRed("You are not logged in!"));
            return false;
        }
    }
}
