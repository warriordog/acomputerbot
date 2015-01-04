package net.acomputerdog.ircbot.command;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.IrcColors;
import com.sorcix.sirc.User;
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
    protected final IrcBot bot;

    private String helpString;
    private CLogger logger;

    public Command(IrcBot bot, String name, String... commands) {
        this.name = name;
        this.bot = bot;
        if (commands == null || commands.length == 0) {
            throw new IllegalArgumentException("Cannot create a command with no command strings!");
        }
        this.commands = commands;
    }

    public Command(IrcBot bot, String command) {
        this(bot, command, command);
    }

    public int getMinArgs() {
        return 0;
    }

    //Not actually maximum, but the maximum that can be used
    public int getMaxArgs() {
        return getMinArgs();
    }

    public boolean allowedInChannel(Channel channel, User user) {
        return true;
    }

    public boolean allowedInPM(User sender) {
        return true;
    }

    public boolean requiresAdmin() {
        return false;
    }

    public boolean canOpOverride() {
        return false;
    }

    public abstract boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command);

    public String getName() {
        return name;
    }

    public String[] getCommands() {
        return commands;
    }

    public String getDescription() {
        return "No description";
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

    //--------Static Stuff-----------------


    public static void onChat(IrcBot bot, Channel channel, User sender, Chattable target, String message) {
        if (message.length() > 1 && message.startsWith(Config.COMMAND_PREFIX)) {
            if (bot.getBlacklist().canUseBot(sender)) {
                CommandLine cmdLine = new CommandLine(message.substring(1));
                Command cmd = commandMap.get(cmdLine.command);
                if (cmd != null) {
                    if (cmd.getMinArgs() <= 0 || cmdLine.hasArgs()) {
                        if (!cmd.requiresAdmin() || bot.getAuth().isAuthenticated(sender) || (cmd.canOpOverride() && sender.hasOperator())) {
                            if (channel == null && cmd.allowedInPM(sender)) {
                                cmd.processCommand(bot, null, sender, target, cmdLine);
                            } else if (cmd.allowedInChannel(channel, sender)) {
                                cmd.processCommand(bot, channel, sender, target, cmdLine);
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
        }
    }

    public static Map<String, Command> getCommandNameMap() {
        return commandNameMap;
    }

    public static Map<String, Command> getCommandMap() {
        return commandMap;
    }

    private static void registerCommand(Command command) {
        if (commandNameMap.containsKey(command.getName())) {
            command.getLogger().logWarning("Registering duplicate command: " + command.getName());
        }
        commandNameMap.put(command.getName(), command);
        for (String cmd : command.getCommands()) {
            Command oldCmd = commandMap.put(cmd.toLowerCase(), command);
            if (oldCmd != null) {
                command.getLogger().logWarning("Overriding command: \"" + oldCmd.getName() + "\" (alias \""  + cmd.toLowerCase() + "\")!");
            }
        }
    }

    public static void init(IrcBot bot) {
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
    }

    protected static String colorRed(String message) {
        return IrcColors.color(message, IrcColors.RED);
    }

    protected static String colorGreen(String text) {
        return IrcColors.color(text, IrcColors.DARK_GREEN);
    }

    protected static String colorYellow(String message) {
        return IrcColors.color(message, IrcColors.YELLOW);
    }
}
