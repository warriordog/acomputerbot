package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

import java.util.Map;

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
    public boolean requiresAdmin() {
        return true;
    }

    @Override
    public boolean canOpOverride() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Tells AcomputerBot to leave a channel.";
    }
    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        if (command.hasArgs()) {
            String channelName = getChannelName(command.args.toLowerCase());
            Map<String, Channel> channelMap = bot.getConnection().getState().getChannelMap();
            if (channelMap.containsKey(channelName)) {
                target.send("Left channel \"" + channelName + "\".");
                channelMap.get(channelName).part();
                return true;
            } else {
                target.send(colorRed("I'm not connected to \"" + channelName + "\"!"));
                return false;
            }
        } else {
            target.send("Bye :(");
            channel.part();
            return true;
        }
    }

    private static String getChannelName(String name) {
        if (!name.startsWith("#")) {
            return "#".concat(name);
        }
        return name;
    }
}
