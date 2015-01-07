package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

import java.util.Set;

public class CommandListWhitelist extends Command {
    public CommandListWhitelist(IrcBot bot) {
        super(bot, "ListWhitelist", "listwhitelist", "list-whitelist", "list_whitelist", "whitelist_list", "whitelist-list");
    }

    @Override
    public boolean requiresAdmin() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Lists whitelisted users.";
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "listwhitelist";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        target.send("Whitelisted users:");
        Set<String> whitelist = bot.getBlacklist().getWhitelistedUsers();
        int count = 0;
        StringBuilder builder = new StringBuilder(20);
        for (String str : whitelist) {
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
