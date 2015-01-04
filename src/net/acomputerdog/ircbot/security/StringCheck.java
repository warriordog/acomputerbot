package net.acomputerdog.ircbot.security;

import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

public class StringCheck {
    private final CLogger LOGGER = new CLogger("StringCheck", false, true);

    private final IrcBot bot;

    public StringCheck(IrcBot bot) {
        this.bot = bot;
    }

    public String filterString(String str) {
        if (Config.CHAT_FILTER_MODE == 0 || str == null || str.isEmpty()) {
            return str;
        } else if (Config.CHAT_FILTER_MODE == 2) {
            return blockCommands(str);
        } else {
            if (Config.CHAT_FILTER_MODE != 1) {
                LOGGER.logError("Invalid chat filter mode: " + Config.CHAT_FILTER_MODE + ".  Assuming mode 1.");
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
            return str.replace(Config.COMMAND_PREFIX, "■");
        }
        return str;
    }
}
