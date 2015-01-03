package net.acomputerdog.ircbot.command;

import com.sorcix.sirc.*;
import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.command.types.*;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

import java.util.HashMap;
import java.util.Map;

public abstract class Command {

    private static final Map<String, Command> commandNameMap = new HashMap<>();
    private static final Map<String, Command> commandMap = new HashMap<>();

    private final String name;
    private final String[] commands;

    private String helpString;
    private CLogger logger;

    public Command(String name, String... commands) {
        this.name = name;
        if (commands == null || commands.length == 0) {
            throw new IllegalArgumentException("Cannot create a command with no command strings!");
        }
        this.commands = commands;
    }

    public Command(String command) {
        this(command, command);
    }

    public int getMinArgs() {
        return 0;
    }

    //Not actually maximum, but the maximum that can be used
    public int getMaxArgs() {
        return getMinArgs();
    }

    public boolean allowedInChannel(Channel channel, User sender) {
        return true;
    }

    public boolean allowedInPM(User sender) {
        return true;
    }

    public abstract boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command);

    /*
    public boolean processCommandGeneric(IrcBot bot, Chattable target, User sender, CommandLine command) {
        getLogger().logWarning("Generic command handler called!");
        return false;
    }

    public abstract boolean processCommandChannel(IrcBot bot, Channel channel, User sender, CommandLine command);
    public abstract boolean processCommandPM(IrcBot bot, User sender, CommandLine command);
    */

    public String getName() {
        return name;
    }

    public String[] getCommands() {
        return commands;
    }

    public String getHelpString() {
        if (helpString == null) {
            StringBuilder builder = new StringBuilder();
            builder.append(Config.COMMAND_PREFIX);
            builder.append(commands[0]);
            if (getMinArgs() > 0) {
                for (int count = 1; count <= getMinArgs(); count++) {
                    builder.append(' ');
                    builder.append("<arg");
                    builder.append(count);
                    builder.append('>');
                }
            }
            if (getMaxArgs() > getMinArgs()) {
                for (int count = getMinArgs(); count <= getMaxArgs(); count++) {
                    builder.append(' ');
                    builder.append("[arg");
                    builder.append(count);
                    builder.append(']');
                }
            }
            helpString = builder.toString();
        }
        return helpString;
    }

    protected CLogger getLogger() {
        if (logger == null) {
            logger = new CLogger("Command" + name, false, true);
        }
        return logger;
    }

    /*
    public static void onChannelChat(IrcBot irc, User sender, Channel target, String message) {
        if (message.startsWith(COMMAND_PREFIX)) {
            CommandLine cmdLine = new CommandLine(message.substring(1));
            Command cmd = commandMap.get(cmdLine.command);
            if (cmd != null) {
                if (cmd.allowedInChannel(target, )) {
                    if (cmd.getMinArgs() <= 0 || cmdLine.hasArgs()) {
                        cmd.processCommandChannel(irc, target, sender, cmdLine);
                    } else {
                        target.send(colorError("Not enough arguments, use \"" + cmd.getHelpString() + "\"."));
                    }
                }
            } else {
                target.send(colorError("Unknown command, use \"" + COMMAND_PREFIX + "\"help for a list of commands."));
            }
        }
    }

    public static void onPrivateMessage(IrcBot irc, User sender, String message) {
        if (message.startsWith(COMMAND_PREFIX)) {
            CommandLine cmdLine = new CommandLine(message.substring(1));
            Command cmd = commandMap.get(cmdLine.command);
            if (cmd != null) {
                if (cmd.allowedInPM(sender)) {
                    if (cmd.getMinArgs() <= 0 || cmdLine.hasArgs()) {
                        cmd.processCommandPM(irc, sender, cmdLine);
                    } else {
                        sender.send(colorError("Not enough arguments, use \"" + cmd.getHelpString() + "\"."));
                    }
                }
            } else {
                sender.send(colorError("Unknown command, use \"" + COMMAND_PREFIX + "help\" for a list of commands."));
            }
        }
    }
*/

    public static void onChat(IrcBot bot, Channel channel, User sender, Chattable target, String message) {
        if (message.length() > 1 && message.startsWith(Config.COMMAND_PREFIX)) {
            CommandLine cmdLine = new CommandLine(message.substring(1));
            Command cmd = commandMap.get(cmdLine.command);
            if (cmd != null) {
                if (cmd.getMinArgs() <= 0 || cmdLine.hasArgs()) {
                    if (channel == null && cmd.allowedInPM(sender)) {
                        cmd.processCommand(bot, null, sender, target, cmdLine);
                    } else if (cmd.allowedInChannel(channel, sender)) {
                        cmd.processCommand(bot, channel, sender, target, cmdLine);
                    }
                } else {
                    target.send(colorError("Not enough arguments, use \"" + cmd.getHelpString() + "\"."));
                }
            } else {
                target.send(colorError("Unknown command, use \"" + Config.COMMAND_PREFIX + "help\" for a list of commands."));
            }
        }
    }

    public static Map<String, Command> getCommandNameMap() {
        return commandNameMap;
    }

    public static Map<String, Command> getCommandMap() {
        return commandMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Command)) return false;

        Command command = (Command) o;

        return name.equals(command.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Command{" +
                "helpString='" + helpString + '\'' +
                '}';
    }

    //--------------------

    private static void registerCommand(Command command) {
        commandNameMap.put(command.getName(), command);
        for (String cmd : command.getCommands()) {
            Command oldCmd = commandMap.put(cmd.toLowerCase(), command);
            if (oldCmd != null) {
                command.getLogger().logWarning("Overriding command: \"" + oldCmd.getName() + "\" (alias \""  + cmd.toLowerCase() + "\")!");
            }
        }
    }

    public static void init() {
        registerCommand(new CommandHelp());
        registerCommand(new CommandInfo());
        registerCommand(new CommandStop());
        registerCommand(new CommandJoin());
        registerCommand(new CommandLeave());
        registerCommand(new CommandSay());
        registerCommand(new CommandSayIn());
        registerCommand(new CommandSayInAll());
        registerCommand(new CommandMe());
        registerCommand(new CommandMeIn());
        registerCommand(new CommandMeInAll());
        registerCommand(new CommandStatus());
        registerCommand(new CommandChannels());
        registerCommand(new CommandGithub());
    }

    protected static String colorError(String message) {
        return IrcColors.color(message, IrcColors.RED);
    }
}
