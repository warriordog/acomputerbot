package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

import java.util.Map;

public class CommandMeInAll extends Command {
    public CommandMeInAll(IrcBot bot) {
        super(bot, "MeInAll", "meinall", "me-in-all", "me_in_all", "actioninall", "action-in-all", "action_in_all", "meall", "me_all", "me-all", "actionall", "action_all", "action-all");
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public boolean requiresAdmin() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Tells AcomputerBot to perform an action (/me) in all connected channels.";
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "MeInAll <message>";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        String filtered = bot.getStringCheck().filterString(command.args);
        if (filtered == null) {
            target.send(colorRed("The string was blocked, probably due to cascaded commands!"));
            return false;
        }
        Map<String, Channel> channelMap = bot.getConnection().getState().getChannelMap();
        for (String chanName : channelMap.keySet()) {
            channelMap.get(chanName).sendAction(filtered);
        }
        return true;
    }
}
