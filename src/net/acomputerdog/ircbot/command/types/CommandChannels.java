package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.main.Channels;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandChannels extends Command {
    public CommandChannels(IrcBot bot) {
        super(bot, "Channels", "channels", "listchannels", "list_channels");
    }

    @Override
    public String getDescription() {
        return "Lists all channels that AcomputerBot is connected to.";
    }

    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        target.send("Connected channels:");
        for (String chan : Channels.getChannels()) {
            String topic = Channels.getChannel(chan).getTopic();
            target.send("  " + chan + (topic == null ? "" : ": " + topic));
        }
        return true;
    }
}
