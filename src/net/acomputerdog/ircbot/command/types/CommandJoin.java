package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

import java.util.Map;

public class CommandJoin extends Command {
    public CommandJoin(IrcBot bot) {
        super(bot, "Join", "join");
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "join <channel>";
    }

    @Override
    public boolean requiresAdmin() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Tells AcomputerBot to join a channel.";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        String channelName = command.args.toLowerCase();
        Map<String, Channel> channelMap = bot.getConnection().getState().getChannelMap();
        if (!channelMap.containsKey(channelName)) {
            Channel chan = bot.getConnection().createChannel(channelName);
            chan.join();
            target.send("Joined \"" + channelName + "\".");
            return true;
        } else {
            target.send(colorRed("Already connected to \"" + channelName + "\"!"));
            return false;
        }
    }
}
