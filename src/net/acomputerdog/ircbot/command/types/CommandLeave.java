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
    public CommandLeave(IrcBot bot) {
        super(bot, "Leave", "leave", "part");
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
    public boolean allowedInChannel(Channel channel, User user) {
        return user.hasOperator() || bot.getAuth().isAuthenticated(user);
    }

    @Override
    public boolean allowedInPM(User user) {
        return bot.getAuth().isAuthenticated(user);
    }


    @Override
    public String getDescription() {
        return "Tells AcomputerBot to leave a channel.";
    }


    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        if (command.hasArgs()) {
            if (bot.getAuth().isAuthenticated(sender)) {
                String channelName = getChannelName(command.args.toLowerCase());
                if (Channels.isConnected(channelName)) {
                    target.send("Left channel \"" + channelName + "\".");
                    Channels.disconnect(channelName);
                    Channels.getChannel(channelName).part();
                    return true;
                } else {
                    target.send(colorRed("I'm not connected to \"" + channelName + "\"!"));
                    return false;
                }
            } else {
                target.send(colorRed("Only channel ops and bot admins may use \"" + Config.COMMAND_PREFIX + "leave\"!"));
                return false;
            }
        } else {
            if (sender.hasOperator() || bot.getAuth().isAuthenticated(sender)) {
                target.send("Bye :(");
                Channels.disconnect(channel.getName().toLowerCase());
                channel.part();
                return true;
            } else {
                target.send(colorRed("Only channel ops and bot admins may use \"" + Config.COMMAND_PREFIX + "leave\"!"));
                return false;
            }
        }
    }

    private static String getChannelName(String name) {
        if (!name.startsWith("#")) {
            return "#".concat(name);
        }
        return name;
    }
}
