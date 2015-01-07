package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.main.IrcBot;

import java.util.Map;

public class CommandChannels extends Command {
    public CommandChannels(IrcBot bot) {
        super(bot, "Channels", "channels", "listchannels", "list_channels");
    }

    @Override
    public String getDescription() {
        return "Lists all channels that AcomputerBot is connected to.";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        target.send("Connected channels:");
        Map<String, Channel> channelMap = bot.getConnection().getState().getChannelMap();
        for (String chan : channelMap.keySet()) {
            String topic = channelMap.get(chan).getTopic();
            target.send("  " + chan + (topic == null ? "" : ": " + topic));
        }
        return true;
    }
}
