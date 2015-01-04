package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.Channels;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandMeIn extends Command {
    public CommandMeIn(IrcBot bot) {
        super(bot, "MeIn", "mein", "me-in", "me_in", "actionin", "action-in", "action_in");
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "MeIn <channel> <message>";
    }

    @Override
    public String getDescription() {
        return "Tells AcomputerBot to perform an action (/me) in a specific channel.";
    }

    @Override
    public boolean requiresAdmin() {
        return true;
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
            String filtered = bot.getStringCheck().filterString(command.args.substring(split + 1));
            if (filtered == null) {
                target.send(colorRed("The string was blocked, probably due to cascaded commands!"));
                return false;
            }
            Channels.getChannel(channelName).sendAction(filtered);
            return true;
        } else {
            target.send(colorRed("Not connected to \"" + channelName + "\"!"));
            return false;
        }
    }
}
