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
        bot.getConnection().sendRaw("/WHO " + user.getNick());
        target.send("Nick: " + user.getNick() + " (" + user.getNickLower() + ")"); //String.valueOf(user.getPrefix() == '0' ? "@" : user.getPrefix()) +
        target.send("RealName: " + user.getRealName());
        target.send("Username: " + user.getUserName());
        target.send("Hostname: " + user.getHostName());
        return true;
    }
}
