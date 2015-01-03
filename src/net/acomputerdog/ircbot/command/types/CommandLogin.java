package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;
import net.acomputerdog.ircbot.security.Auth;

public class CommandLogin extends Command {
    public CommandLogin() {
        super("Login", "login", "verify", "log-in", "log_in", "identify", "authenticate", "auth", "authorize");
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "login <password>";
    }

    @Override
    public boolean allowedInChannel(Channel channel, User user) {
        return false;
    }

    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        target.send("Authentication request processed.  Please wait to be authenticated...");
        Auth.requestAuthentication(sender, command.args);
        return true;
    }
}
