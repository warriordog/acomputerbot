package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

import java.util.Set;

public class CommandListBlacklist extends Command {
    public CommandListBlacklist(IrcBot bot) {
        super(bot, "ListBlacklist", "listblacklist", "list-blacklist", "list_blacklist", "blacklist_list", "blacklist-list");
    }

    @Override
    public boolean requiresAdmin() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Lists blacklisted users.";
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "listblacklist";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        target.send("Blacklisted users:");
        Set<String> blacklist = bot.getBlacklist().getBlacklistedUsers();
        int count = 0;
        StringBuilder builder = new StringBuilder(20);
        for (String str : blacklist) {
            if (count > 0) {
                builder.append(", ");
            } else {
                builder.append("  ");
            }
            builder.append(str);
            count++;
            if (count >= 10) {
                target.send(builder.toString());
                builder = new StringBuilder(20);
                count = 0;
            }
        }
        String lastLine = builder.toString();
        if (!lastLine.isEmpty()) {
            target.send(colorGreen(lastLine));
        }
        return true;
    }
}
