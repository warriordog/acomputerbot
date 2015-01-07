package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
import net.acomputerdog.core.java.Patterns;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

import java.util.Deque;
import java.util.LinkedList;

public class CommandPipe extends Command {
    private String lastResponse = "";

    public CommandPipe(IrcBot bot) {
        super(bot, "Pipe", "pipe", "link", "joincmd", "join-cmd", "join_cmd", "|");
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "pipe <commands>";
    }

    @Override
    public String getDescription() {
        return "Allows commands to be connected using \"|\" symbols.";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        String[] commands = command.args.split(Patterns.quote("|"));
        Deque<String> commandDeque = new LinkedList<>();
        for (String cmd : commands) {
            if (!cmd.startsWith(Config.COMMAND_PREFIX)) {
                cmd = Config.COMMAND_PREFIX + cmd;
            }
            commandDeque.addFirst(cmd);
        }

        lastResponse = "";
        Chattable responseReader = new Chattable() {
            @Override
            public void send(String message) {
                lastResponse = lastResponse + message + "\n";
            }

            @Override
            public void sendAction(String action) {
                lastResponse = lastResponse + action + "\n";
            }

            @Override
            public String getName() {
                return "CommandPipe[" + target.getName() + "]";
            }
        };

        for (String cmd : commandDeque) {
            String fullCmd = (cmd + " " + lastResponse).trim();
            lastResponse = "";
            Command.onChat(bot, channel, sender, responseReader, fullCmd);
        }
        bot.getStringCheck().sendFormattedString(lastResponse, target);
        return true;
    }
}
