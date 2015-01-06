package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandStatus extends Command {
    public CommandStatus(IrcBot bot) {
        super(bot, "Status", "status", "stats");
    }

    @Override
    public String getDescription() {
        return "Gets the internal status of AcomputerBot.";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        target.send(bot.getVersionString() + " status:");
        target.send("  " + Command.getCommandNameMap().size() + " loaded commands with " + Command.getCommandMap().size() + " registered aliases.");
        target.send("  Memory: " + (Runtime.getRuntime().freeMemory() / 1000000) + "mb used / " + (Runtime.getRuntime().totalMemory() / 1000000) + "mb allocated / " + (Runtime.getRuntime().maxMemory() / 1000000) + "mb available.");
        return true;
    }
}
