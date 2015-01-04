package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandEnableBlacklist extends Command {
    public CommandEnableBlacklist(IrcBot bot) {
        super(bot, "EnableBlacklist", "enableblacklist", "enable-blacklist", "enable_blacklist", "blackliston", "blacklist-on", "blacklist_on");
    }

    @Override
    public boolean requiresAdmin() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Toggles blacklist mode.";
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "enableblacklist";
    }

    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        if (Config.ENABLE_BLACKLIST) {
            Config.ENABLE_BLACKLIST = false;
            target.send("Blacklist disabled.");
        } else {
            Config.ENABLE_BLACKLIST = true;
            target.send("Blacklist enabled.");
        }
        return true;
    }
}
