package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.Channels;
import net.acomputerdog.ircbot.main.IrcBot;

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
    public boolean allowedInPM(User user) {
        return bot.getAuth().isAuthenticated(user);
    }

    @Override
    public boolean allowedInChannel(Channel channel, User user) {
        return bot.getAuth().isAuthenticated(user);
    }

    @Override
    public String getDescription() {
        return "Tells AcomputerBot to join a channel.";
    }

    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        String channelName = command.args.toLowerCase();
        if (!Channels.isConnected(channelName)) {
            Channel chan = bot.getConnection().createChannel(channelName);
            chan.join();
            Channels.connect(chan);
            target.send("Joined \"" + channelName + "\".");
            return true;
        } else {
            target.send(colorRed("Already connected to \"" + channelName + "\"!"));
            return false;
        }
    }
}
