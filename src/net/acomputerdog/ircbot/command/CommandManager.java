package net.acomputerdog.ircbot.command;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
import com.sorcix.sirc.util.IrcColors;
import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private final IrcBot bot;

    private final CLogger LOGGER;
    private final Map<String, Command> commandNameMap = new HashMap<>();
    private final Map<String, Command> commandMap = new HashMap<>();

    private boolean commandInProgress = false;

    public CommandManager(IrcBot bot) {
        this.bot = bot;
        LOGGER = bot.getLogManager().getLogger("CommandManager");
    }

    public void registerCommand(Command command) {
        if (commandNameMap.containsKey(command.getName())) {
            command.getLogger().logWarning("Registering duplicate command: " + command.getName());
        }
        commandNameMap.put(command.getName(), command);
        for (String cmd : command.getCommands()) {
            Command oldCmd = commandMap.put(cmd.toLowerCase(), command);
            if (oldCmd != null) {
                command.getLogger().logWarning("Overriding command: \"" + oldCmd.getName() + "\" (alias \"" + cmd.toLowerCase() + "\")!");
            }
        }
    }

    public void onChat(Channel channel, User sender, Chattable target, String message) {
        commandInProgress = true;
        try {
            if (message.length() > 1 && message.startsWith(Config.COMMAND_PREFIX)) {
                CommandLine cmdLine = new CommandLine(message.substring(1));
                Command cmd = commandMap.get(cmdLine.command);
                try {
                    if (bot.getBlacklist().canUseBot(sender) || cmd.canOverrideBlacklist() || (cmd.canOpOverride() && sender.hasOperator())) {
                        if (cmd != null) {
                            if (cmd.getMinArgs() <= 0 || cmdLine.hasArgs()) {
                                if (!cmd.requiresAdmin() || bot.getAuth().isAuthenticated(sender) || (cmd.canOpOverride() && sender.hasOperator())) {
                                    if (channel == null && cmd.allowedInPM(sender)) {
                                        cmd.processCommand(null, sender, target, cmdLine);
                                        cmd.getLogger().logInfo("User " + sender.getNick() + " used command in PM: \"" + message + "\".");
                                    } else if (channel != null && cmd.allowedInChannel(channel, sender)) {
                                        cmd.processCommand(channel, sender, target, cmdLine);
                                        cmd.getLogger().logInfo("User " + sender.getNick() + " used command in " + channel.getName() + ": \"" + message + "\".");
                                    } else {
                                        target.send(colorRed("That command cannot be used here!"));
                                    }
                                } else {
                                    if (cmd.canOpOverride()) {
                                        target.send(colorRed("Only a bot admin or channel operator can perform that command!"));
                                    } else {
                                        target.send(colorRed("Only a bot admin can perform that command!"));
                                    }
                                }
                            } else {
                                target.send(colorRed("Not enough arguments, use \"" + cmd.getHelpString() + "\"."));
                            }
                        } else {
                            target.send(colorRed("Unknown command, use \"" + Config.COMMAND_PREFIX + "help\" for a list of commands."));
                        }
                    } else {
                        target.send(colorRed("You are not permitted to use AcomputerBot!"));
                    }
                } catch (Exception e) {
                    if (cmd == null) {
                        LOGGER.logError("Null command: " + cmdLine.command);
                    } else {
                        cmd.getLogger().logError("An exception occurred while processing this command!");
                        cmd.getLogger().logError("The command being executed was \"" + message + "\".");
                        cmd.getLogger().logError("The exception was a \"" + e.getClass().getName() + "\".  Message: \"" + e.getMessage() + "\".", e);
                        target.send(colorRed("An exception occurred while processing the command!  Please report this!"));
                    }
                }
            }
        } catch (Throwable t) {
            try {
                LOGGER.logError("Uncaught exception while executing command!");
                LOGGER.logError("Command executed was \"" + (channel == null ? "NULL" : channel.getName()) + "/" + (sender == null ? "NULL" : sender.getNick()) + ": '" + message + "'\".");
                LOGGER.logError("Exception was a \"" + t.getClass().getName() + "\".", t);
            } catch (Throwable t2) {
                try {
                    PrintStream err = System.err;
                    if (err != null) {
                        printSevereError(err);
                    } else {
                        PrintStream out = System.out;
                        if (out != null) {
                            printSevereError(out);
                        } else {
                            // What's going on?  This can't be happening!
                        }
                    }
                } catch (Throwable t3) {
                    // This isn't even funny.
                }
            }
        }
        commandInProgress = false;
    }

    private static void printSevereError(PrintStream out) {
        out.println("-----------------------------------------------------------------------------------------------------------------");
        out.println("[Command] An exception occurred while handling an unhandled exception that occurred while processing a command!");
        out.println("[Command] This should NEVER happen, please report this!");
        out.println("[Command] If this problem recurs then reboot your system and check your java for corruption!");
        out.println("-----------------------------------------------------------------------------------------------------------------");
    }


    public Map<String, Command> getCommandNameMap() {
        return commandNameMap;
    }

    public Map<String, Command> getCommandMap() {
        return commandMap;
    }

    public boolean isCommandInProgress() {
        return commandInProgress;
    }

    private static String colorRed(String message) {
        return IrcColors.color(message, IrcColors.RED);
    }
}
