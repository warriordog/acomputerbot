package net.acomputerdog.ircbot.command;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
import com.sorcix.sirc.util.IrcColors;
import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.command.types.*;
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
                                    } else if (cmd.allowedInChannel(channel, sender)) {
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
                    cmd.getLogger().logError("An exception occurred while processing this command!");
                    cmd.getLogger().logError("The command being executed was \"" + message + "\".");
                    cmd.getLogger().logError("The exception was a \"" + e.getClass().getName() + "\".  Message: \"" + e.getMessage() + "\".", e);
                    target.send(colorRed("An exception occurred while processing the command!  Please report this!"));
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
        out.println("[Command] This should NEVER happen, please report this and reboot your system!");
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

    public void init() {
        registerCommand(new CommandHelp(bot));
        registerCommand(new CommandInfo(bot));
        registerCommand(new CommandStop(bot));
        registerCommand(new CommandJoin(bot));
        registerCommand(new CommandLeave(bot));
        registerCommand(new CommandSay(bot));
        registerCommand(new CommandSayIn(bot));
        registerCommand(new CommandSayInAll(bot));
        registerCommand(new CommandMe(bot));
        registerCommand(new CommandMeIn(bot));
        registerCommand(new CommandMeInAll(bot));
        registerCommand(new CommandStatus(bot));
        registerCommand(new CommandChannels(bot));
        registerCommand(new CommandGithub(bot));
        registerCommand(new CommandLogin(bot));
        registerCommand(new CommandLogout(bot));
        registerCommand(new CommandSpyOn(bot));
        registerCommand(new CommandSpyIn(bot));
        registerCommand(new CommandAdmins(bot));
        registerCommand(new CommandAliases(bot));
        registerCommand(new CommandWhitelist(bot));
        registerCommand(new CommandBlacklist(bot));
        registerCommand(new CommandToggleWhitelist(bot));
        registerCommand(new CommandToggleBlacklist(bot));
        registerCommand(new CommandListWhitelist(bot));
        registerCommand(new CommandListBlacklist(bot));
        registerCommand(new CommandSmiley(bot));
        registerCommand(new CommandChar(bot));
        registerCommand(new CommandSudo(bot));
        registerCommand(new CommandSudoPrivate(bot));
        registerCommand(new CommandPrivateMessage(bot));
        registerCommand(new CommandWhoAmI(bot));
        registerCommand(new CommandJavaScript(bot));
        registerCommand(new CommandPipe(bot));
        registerCommand(new CommandJavaScriptConsole(bot));
        registerCommand(new CommandNote(bot));
        registerCommand(new CommandClearNotes(bot));
        registerCommand(new CommandReload(bot));
    }

    private static String colorRed(String message) {
        return IrcColors.color(message, IrcColors.RED);
    }
}
