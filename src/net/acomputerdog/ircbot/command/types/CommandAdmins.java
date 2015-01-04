package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.main.IrcBot;

public class CommandAdmins extends Command {
    public CommandAdmins(IrcBot bot) {
        super(bot, "Admins", "admins", "listadmins", "list-admins", "list_admins", "listadmin", "list-admin", "list_admin");
    }

    @Override
    public String getDescription() {
        return "Lists registered bot admins.";
    }

    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        sendOnlineAdmins(target);
        if (bot.getAuth().isAuthenticated(sender)) {
            sendRegisteredAdmins(target);
        }
        return true;
    }

    private void sendOnlineAdmins(Chattable target) {
        target.send("Online admins:");
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (String admin : bot.getAdmins().getAdmins()) {
            if (bot.getAuth().isAuthenticated(bot.getConnection().createUser(admin))) {
                if (count > 0) {
                    builder.append(", ");
                } else {
                    builder.append("  ");
                }
                builder.append(admin);
                count++;
            }
        }
        String onlineString = builder.toString();
        if (onlineString.isEmpty()) {
            onlineString = "  No admins are online :(";
        }
        target.send(onlineString);
    }

    private void sendRegisteredAdmins(Chattable target) {
        target.send("Registered admins:");
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (String admin : bot.getAdmins().getAdmins()) {
            if (count > 0) {
                builder.append(", ");
            } else {
                builder.append("  ");
            }
            builder.append(admin);
            count++;
        }
        String registeredString = builder.toString();
        if (registeredString.isEmpty()) {
            registeredString = "  No admins are registered :(";
        }
        target.send(registeredString);
    }
}
