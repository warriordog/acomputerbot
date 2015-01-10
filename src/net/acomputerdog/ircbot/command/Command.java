package net.acomputerdog.ircbot.command;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
import com.sorcix.sirc.util.IrcColors;
import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

public abstract class Command {

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

    public abstract boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command);

    public String getName() {
        return name;
    }

    public String[] getCommands() {
        return commands;
    }

    public String getDescription() {
        return "No description";
    }

    public boolean canOverrideBlacklist() {
        return false;
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
            logger = bot.getLogManager().getLogger("Command" + name);
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
