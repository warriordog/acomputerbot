package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandHelp extends Command {
    public CommandHelp(IrcBot bot) {
        super(bot, "Help", "help", "hlp", "?");
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "help [command]";
    }

    @Override
    public String getDescription() {
        return "Gets help on AcomputerBot commands.";
    }

    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        if (command.hasArgs()) {
            Command cmd = getCommandMap().get(command.args.toLowerCase());
            if (cmd != null) {
                target.send(colorGreen("Found command: " + cmd.getName()));
                target.send(colorGreen("  Usage: \"" + cmd.getHelpString() + "\""));
                target.send(colorGreen("  Aliases: " + getAliases(cmd)));
                target.send(colorGreen("  Description: " + cmd.getDescription()));
                target.send(colorGreen("  You can use: " + canUse(channel, sender, cmd)));
                return true;
            } else {
                target.send(colorRed("Could not find command: \"" + command.args + "\""));
                return false;
            }
        } else {
            target.send(colorGreen("Registered commands: (use \"" + getHelpString() + "\" to view details)"));
            StringBuilder builder = new StringBuilder();
            builder.append("  ");
            int count = 0;
            for (String cmd : getCommandNameMap().keySet()) {
                Command cmmd = getCommandNameMap().get(cmd);
                if ((cmmd.requiresAdmin() || bot.getAuth().isAuthenticated(sender) || (cmmd.canOpOverride() && sender.hasOperator())) &&
                        ((channel != null && cmmd.allowedInChannel(channel, sender)) || (channel == null && cmmd.allowedInPM(sender)))) {
                    if (count > 0) {
                        builder.append(", ");
                    }
                    builder.append(cmd);
                    count++;
                }
            }
            target.send(colorGreen(builder.toString()));

            return true;
        }
    }

    private String getAliases(Command cmd) {
        String[] cmds = cmd.getCommands();
        if (cmds.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(cmds.length);
        for (int index = 0; index < cmds.length; index++) {
            builder.append(cmds[index]);
            if (index < cmds.length - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    private boolean canUse(Channel channel, User user, Command command) {
        if (channel == null) {
            return command.allowedInPM(user);
        }
        return command.allowedInChannel(channel, user);
    }
}
