package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandSay extends Command {
    public CommandSay(IrcBot bot) {
        super(bot, "Say", "say");
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "Tells AcomputerBot to say something.";
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "say <message>";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        String filtered = bot.getStringCheck().filterString(command.args);
        if (filtered == null) {
            target.send(colorRed("The string was blocked, probably due to cascaded commands!"));
            return false;
        }
        target.send(filtered);
        return true;
    }
}
