package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.Channels;
import net.acomputerdog.ircbot.main.IrcBot;
import net.acomputerdog.ircbot.security.Auth;

public class CommandSayInAll extends Command {
    public CommandSayInAll(IrcBot bot) {
        super(bot, "SayInAll", "sayinall", "say-in-all", "say_in_all", "sayall", "say-all", "say_all");
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public boolean allowedInChannel(Channel channel, User user) {
        return bot.getAuth().isAuthenticated(user);
    }

    @Override
    public boolean allowedInPM(User user) {
        return bot.getAuth().isAuthenticated(user);
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "sayinall <message>";
    }

    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        for (String chanName : Channels.getChannels()) {
            Channels.getChannel(chanName).send(command.args);
        }
        return true;
    }
}
