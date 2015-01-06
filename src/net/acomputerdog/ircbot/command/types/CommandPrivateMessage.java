package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandPrivateMessage extends Command {
    public CommandPrivateMessage(IrcBot bot) {
        super(bot, "PrivateMessage", "privatemessage", "private-message", "private_message", "pm", "sendpm", "send-pm", "send_pm", "msg", "tell");
    }

    @Override
    public String getDescription() {
        return "Sends a PM to a player";
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "privatemessage <player> <message>";
    }

    @Override
    public boolean requiresAdmin() {
        return true;
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        int index = command.args.indexOf(' ');
        if (index != -1) {
            String user = command.args.substring(0, index);
            String message = command.args.substring(index + 1);
            User usr = bot.getConnection().createUser(user);
            bot.getStringCheck().sendFormattedString(message, usr, true);
            target.send("Private message sent.");
            return true;
        } else {
            target.send(colorRed("Not enough args, use \"" + getHelpString() + "\"!"));
            return false;
        }
    }
}
