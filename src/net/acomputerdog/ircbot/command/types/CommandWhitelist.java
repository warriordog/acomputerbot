package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandWhitelist extends Command {
    public CommandWhitelist(IrcBot bot) {
        super(bot, "Whitelist", "whitelist", "addwhitelist", "add_whitelist", "add-whitelist");
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "whitelist <user>";
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
        return "Changes a player's whitelist status.";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        User user = bot.getConnection().createUser(command.args);
        if (bot.getBlacklist().isWhitelisted(user)) {
            bot.getBlacklist().removeWhitelisted(user);
            target.send("User " + user.getNick() + " has been un-whitelisted.");
        } else {
            bot.getBlacklist().addWhitelisted(user);
            target.send("User " + user.getNick() + " has been whitelisted.");
        }
        return true;
    }
}
