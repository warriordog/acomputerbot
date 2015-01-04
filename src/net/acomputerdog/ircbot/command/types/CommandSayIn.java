package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.Channels;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandSayIn extends Command {
    public CommandSayIn(IrcBot bot) {
        super(bot, "SayIn", "sayin", "say-in", "say_in");
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "sayin <channel> <message>";
    }
    @Override
    public boolean allowedInChannel(Channel channel, User user) {
        return bot.getAuth().isAuthenticated(user);
    }

    @Override
    public String getDescription() {
        return "Tells AcomputerBot to say something in a channel.";
    }

    @Override
    public boolean allowedInPM(User user) {
        return bot.getAuth().isAuthenticated(user);
    }

    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        int split = command.args.indexOf(' ');
        if (split == -1 || split == command.args.length() - 1) {
            target.send(colorRed("Not enough args, use \"" + getHelpString() + "\"."));
            return false;
        }
        String channelName = command.args.substring(0, split).toLowerCase();
        if (Channels.isConnected(channelName)) {
            Channels.getChannel(channelName).send(command.args.substring(split + 1));
            return true;
        } else {
            target.send(colorRed("Not connected to \"" + channelName + "\"!"));
            return false;
        }
    }
}
