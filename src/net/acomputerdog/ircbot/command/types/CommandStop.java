package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandStop extends Command {
    public CommandStop(IrcBot bot) {
        super(bot, "Stop", "stop", "shutdown");
    }

    @Override
    public boolean requiresAdmin() {
        return true;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "stop [reason]";
    }

    @Override
    public String getDescription() {
        return "Shuts down AcomputerBot.";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        target.send("Bye bye!");
        if (command.hasArgs()) {
            bot.stop(command.args);
        } else {
            bot.stop();
        }
        return true;
    }
}
