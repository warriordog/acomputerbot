package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandWhoAmI extends Command {
    public CommandWhoAmI(IrcBot bot) {
        super(bot, "WhoAmI", "whoami", "who-am-i", "who_am_i");
    }

    @Override
    public String getDescription() {
        return "Returns information about the caller.";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        User user;
        if (channel != null) {
            user = bot.getConnection().createUser(sender.getNickLower(), channel.getName());
        } else {
            user = sender;
        }
        target.send(colorRed("Warning: This command is unfinished, and will retrieve only the nick, realname, and lowernick for the user."));
        target.send("Nick: " + user.getNick() + " (" + String.valueOf(user.getPrefix()) + user.getNickLower() + ")");
        target.send("RealName: " + user.getRealName());
        target.send("Username: " + user.getUserName());
        target.send("Hostname: " + user.getHostName());
        return true;
    }
}
