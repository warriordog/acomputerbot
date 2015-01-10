package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
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
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        target.send("Registered aliases:");
        StringBuilder builder = new StringBuilder(16);
        int count = 0;
        for (String str : bot.getCommandManager().getCommandMap().keySet()) {
            if (count > 0) {
                builder.append(", ");
            } else {
                builder.append("  ");
            }
            builder.append(str);
            count++;
            if (count >= 8) {
                target.send(builder.toString());
                builder = new StringBuilder(16);
                count = 0;
            }
        }
        String lastLine = builder.toString();
        if (!lastLine.isEmpty()) {
            target.send(lastLine);
        }
        return true;
    }
}
