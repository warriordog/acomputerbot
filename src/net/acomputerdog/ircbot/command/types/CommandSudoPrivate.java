package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandSudoPrivate extends Command {
    public CommandSudoPrivate(IrcBot bot) {
        super(bot, "SudoPrivate", "sudoprivate", "runasprivate", "run-as-private", "run_as_private", "sudop");
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public String getDescription() {
        return "Runs a command as another user (as if from a PM).";
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "sudop <user> <command>";
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
            String cmd = getCommand(command.args.substring(index + 1));
            User usr = bot.getConnection().createUser(user);
            bot.getCommandManager().onChat(channel, usr, usr, cmd);
            return true;
        } else {
            target.send(colorRed("Not enough args, use \"" + getHelpString() + "\"!"));
            return false;
        }
    }

    private String getCommand(String cmd) {
        if (!cmd.startsWith(Config.COMMAND_PREFIX)) {
            return Config.COMMAND_PREFIX + cmd;
        }
        return cmd;
    }
}
