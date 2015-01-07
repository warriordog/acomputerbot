package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandLogin extends Command {
    public CommandLogin(IrcBot bot) {
        super(bot, "Login", "login", "verify", "log-in", "log_in", "identify", "authenticate", "auth", "authorize");
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public boolean canOverrideBlacklist() {
        return true;
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "login <password>";
    }

    @Override
    public String getDescription() {
        return "Login as an AcomputerBot admin.";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        target.send("Authentication request processed.  Please wait to be authenticated...");
        bot.getAuth().requestAuthentication(sender, target, command.args);
        return true;
    }
}
