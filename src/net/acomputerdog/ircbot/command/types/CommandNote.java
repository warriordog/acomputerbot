package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

import java.util.HashMap;
import java.util.Map;

public class CommandNote extends Command {
    private final Map<User, String> noteMap;

    public CommandNote(IrcBot bot) {
        super(bot, "Note", "note");
        noteMap = new HashMap<>();
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "note [contents]";
    }

    @Override
    public String getDescription() {
        return "Saves or reads a note for the user.";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        if (command.hasArgs()) {
            noteMap.put(sender, command.args);
            target.send("Note saved successfully.");
            return true;
        } else {
            String note = noteMap.get(sender);
            if (note == null || note.isEmpty()) {
                target.send(colorRed("You have no note saved!"));
                return false;
            } else {
                bot.getStringCheck().sendFormattedString(note, target, "", true);
                return true;
            }
        }
    }

    public Map<User, String> getNoteMap() {
        return noteMap;
    }
}
