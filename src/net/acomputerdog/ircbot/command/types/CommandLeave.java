package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.Channels;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandLeave extends Command {
    public CommandLeave() {
        super("Leave", "leave", "part");
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "leave [channel]";
    }

    @Override
    public boolean allowedInChannel(Channel channel, User sender) {
        return sender.hasOperator() || "acomputerdog".equals(sender.getRealName());
    }

    @Override
    public boolean allowedInPM(User user) {
        return "acomputerdog".equals(user.getRealName());
    }

    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        if (channel == null) {
            if (!command.hasArgs()) {
                target.send(colorError("You must specify a channel to leave!"));
                return false;
            } else {
                String channelName = getChannelName(command.args.toLowerCase());
                if (Channels.isConnected(channelName)) {
                    target.send("Left channel \"" + channelName + "\".");
                    Channels.getChannel(channelName).part();
                    Channels.disconnect(channelName);
                    return true;
                } else {
                    target.send(colorError("I'm not connected to \"" + channelName + "\"!"));
                    return false;
                }
            }
        } else if (sender.hasOperator() || "acomputerdog".equals(sender.getRealName())) {
            target.send("Bye :(");
            channel.part();
            Channels.disconnect(channel.getName().toLowerCase());
            return true;
        } else {
            target.send(colorError("Only channel ops and bot admins may use \"" + Config.COMMAND_PREFIX + "leave\"!"));
            return false;
        }
    }

    private static String getChannelName(String name) {
        if (!name.startsWith("#")) {
            return "#".concat(name);
        }
        return name;
    }
}
