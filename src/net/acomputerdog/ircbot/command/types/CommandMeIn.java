package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Admins;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.Channels;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandMeIn extends Command {
    public CommandMeIn() {
        super("MeIn", "mein", "me-in", "me_in", "actionin", "action-in", "action_in");
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
    public boolean allowedInChannel(Channel channel, User user) {
        return Admins.isAdmin(user);
    }

    @Override
    public boolean allowedInPM(User user) {
        return Admins.isAdmin(user);
    }

    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        int split = command.args.indexOf(' ');
        if (split == -1 || split == command.args.length() - 1) {
            target.send(colorError("Not enough args, use \"" + getHelpString() + "\"."));
            return false;
        }
        String channelName = command.args.substring(0, split).toLowerCase();
        if (Channels.isConnected(channelName)) {
            Channels.getChannel(channelName).sendAction(command.args.substring(split + 1));
            return true;
        } else {
            target.send(colorError("Not connected to \"" + channelName + "\"!"));
            return false;
        }
    }
}
