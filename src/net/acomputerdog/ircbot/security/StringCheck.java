package net.acomputerdog.ircbot.security;

import com.sorcix.sirc.util.Chattable;
import net.acomputerdog.core.java.Patterns;
import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

public class StringCheck {
    private final CLogger LOGGER;

    private final IrcBot bot;

    public StringCheck(IrcBot bot) {
        this.bot = bot;
        LOGGER = bot.getLogManager().getLogger("StringCheck");
    }

    public boolean sendFormattedString(String str, Chattable target) {
        return sendFormattedString(str, target, false);
    }

    public boolean sendFormattedString(String str, Chattable target, boolean filter) {
        return sendFormattedString(str, target, "", filter);
    }

    public boolean sendFormattedString(String str, Chattable target, String prefix, boolean filter) {
        str = filter ? filterString(str) : str;
        if (str != null) {
            String[] lines = str.split(Patterns.NEWLINE);
            if (lines.length <= 5 || !filter) {
                for (String line : lines) {
                    if (line.length() > 400 && filter) {
                        target.send("Output was too long, so it has been truncated.");
                        line = line.substring(0, 397).concat("...");
                    }
                    target.send(prefix + line);
                }
            } else {
                target.send("Output had too many lines, so newlines were replaced with \"▼\".");
                if (str.length() > 400) {
                    target.send("Output was too long, so it has been truncated.");
                    str = str.substring(0, 397).concat("...");
                }
                target.send(prefix + str.replace("\n", "▼"));
            }
        }
        return false;
    }

    public String filterString(String str) {
        str = filterCascadedCommands(str);
        return str;
    }

    private String filterCascadedCommands(String str) {
        if (Config.COMMAND_FILTER_MODE == 0 || str == null || str.isEmpty()) {
            return str;
        } else if (Config.COMMAND_FILTER_MODE == 2) {
            return blockCommands(str);
        } else {
            if (Config.COMMAND_FILTER_MODE != 1) {
                LOGGER.logError("Invalid chat filter mode: " + Config.COMMAND_FILTER_MODE + ".  Assuming mode 1.");
            }
            return filterCommands(str);
        }
    }

    private String blockCommands(String str) {
        char[] strChars = str.toCharArray();
        char[] commandChars = Config.COMMAND_PREFIX.toCharArray();
        int numCommands = 0;
        for (int index = 0; index < strChars.length; index++) {
            char chr = strChars[index];
            notCommand:
            if (chr == commandChars[0]) {
                for (int commandIndex = 1; commandIndex < commandChars.length; commandIndex++) {
                    int strIndex = index + commandIndex;
                    if (strIndex >= strChars.length || strChars[strIndex] != commandChars[commandIndex]) {
                        index = strIndex;
                        break notCommand;
                    }
                }
                numCommands++;
            }
            if (numCommands > Config.MAX_CASCADED_COMMANDS) {
                return null;
            }
        }
        return str;
    }

    private String filterCommands(String str) {
        String blocked = blockCommands(str);
        if (blocked == null) {
            return str.replace(Config.COMMAND_PREFIX, "□");
        }
        return str;
    }
}
