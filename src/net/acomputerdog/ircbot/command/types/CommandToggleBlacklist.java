package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandToggleBlacklist extends Command {
    public CommandToggleBlacklist(IrcBot bot) {
        super(bot, "ToggleBlacklist", "toggleblacklist", "toggle-blacklist", "toggle_blacklist");
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
        return Config.COMMAND_PREFIX + "toggleblacklist";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
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
