package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandToggleWhitelist extends Command {
    public CommandToggleWhitelist(IrcBot bot) {
        super(bot, "ToggleWhitelist", "togglewhitelist", "toggle-whitelist", "toggle_whitelist");
    }

    @Override
    public boolean requiresAdmin() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Toggles whitelist mode.";
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "togglewhitelist";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        if (Config.ENABLE_WHITELIST) {
            Config.ENABLE_WHITELIST = false;
            target.send("Whitelist disabled.");
        } else {
            Config.ENABLE_WHITELIST = true;
            target.send("Whitelist enabled.");
        }
        return true;
    }
}
