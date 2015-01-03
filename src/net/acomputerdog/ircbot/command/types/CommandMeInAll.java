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
import net.acomputerdog.ircbot.security.Auth;

public class CommandMeInAll extends Command {
    public CommandMeInAll() {
        super("MeInAll", "meinall", "me-in-all", "me_in_all", "actioninall", "action-in-all", "action_in_all", "meall", "me_all", "me-all", "actionall", "action_all", "action-all");
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public boolean allowedInChannel(Channel channel, User user) {
        return Auth.isAuthenticated(user);
    }

    @Override
    public boolean allowedInPM(User user) {
        return Auth.isAuthenticated(user);
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "MeInAll <message>";
    }

    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        for (String chanName : Channels.getChannels()) {
            Channels.getChannel(chanName).sendAction(command.args);
        }
        return true;
    }
}
