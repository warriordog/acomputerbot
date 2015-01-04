package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandAliases extends Command {
    public CommandAliases(IrcBot bot) {
        super(bot, "Aliases", "aliases", "list-aliases", "list_aliases", "alias", "list-alias", "list_alias");
    }

    @Override
    public String getDescription() {
        return "Prints out all registered command aliases.";
    }

    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        target.send("Registered aliases:");
        StringBuilder builder = new StringBuilder(20);
        int count = 0;
        for (String str : getCommandMap().keySet()) {
            if (count > 0) {
                builder.append(", ");
            } else {
                builder.append("  ");
            }
            builder.append(str);
            count++;
            if (count >= 10) {
                target.send(builder.toString());
                builder = new StringBuilder(20);
                count = 0;
            }
        }
        target.send(builder.toString());
        return true;
    }
}
