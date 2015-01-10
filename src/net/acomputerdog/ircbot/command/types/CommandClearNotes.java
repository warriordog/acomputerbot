package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

import java.util.Map;

public class CommandClearNotes extends Command {
    private Map<User, String> noteMap;

    public CommandClearNotes(IrcBot bot) {
        super(bot, "ClearNotes", "clearnotes", "clear-notes", "clear_notes", "wipenotes", "wipe-notes", "wipe_notes", "erasenotes", "erase-notes", "erase_notes");
    }

    @Override
    public boolean requiresAdmin() {
        return true;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "clearnotes [user]";
    }

    @Override
    public String getDescription() {
        return "Clears stored notes for one or all users.";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        if (noteMap == null) {
            noteMap = ((CommandNote) bot.getCommandManager().getCommandNameMap().get("Note")).getNoteMap();
        }
        if (command.hasArgs()) {
            User user = bot.getConnection().createUser(command.args);
            if (noteMap.containsKey(user)) {
                noteMap.remove(user);
                target.send("Cleared note for " + command.args + ".");
                return true;
            } else {
                target.send(colorRed("No stored note for " + command.args + "!"));
                return false;
            }
        } else {
            noteMap.clear();
            target.send("Cleared all notes.");
            return true;
        }
    }
}
