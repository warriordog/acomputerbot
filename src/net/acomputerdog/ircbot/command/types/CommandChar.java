package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandChar extends Command {
    public CommandChar(IrcBot bot) {
        super(bot, "Char", "char", "character", "getchar", "get-char", "get_char");
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "char <character_id>";
    }

    @Override
    public String getDescription() {
        return "Prints a specified unicode character into chat.";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        try {
            target.send(String.valueOf((char) Integer.parseInt(command.args, 16)));
            return true;
        } catch (NumberFormatException e) {
            target.send(colorRed("You must enter an hexadecimal unicode character id!"));
            return false;
        }
    }
}
