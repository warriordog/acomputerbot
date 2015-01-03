package net.acomputerdog.ircbot.security;

import com.sorcix.sirc.*;
import net.acomputerdog.core.java.Patterns;
import net.acomputerdog.ircbot.main.IrcBot;

public class NickservListener implements MessageListener {

    private final IrcBot bot;
    private final User nickServ;

    public NickservListener(IrcBot bot) {
        this.bot = bot;
        nickServ = bot.getConnection().createUser("NickServ");
    }

    public User getNickServ() {
        return nickServ;
    }

    @Override
    public void onAction(IrcConnection irc, User sender, Channel target, String action) {}

    @Override
    public void onAction(IrcConnection irc, User sender, String action) {}

    @Override
    public void onCtcpReply(IrcConnection irc, User sender, String command, String message) {}

    @Override
    public void onMessage(IrcConnection irc, User sender, Channel target, String message) {}

    @Override
    public void onNotice(IrcConnection irc, User sender, Channel target, String message) {}

    @Override
    public void onNotice(IrcConnection irc, User sender, String message) {
        if (sender.getRealName().equals("NickServ")) {
            String[] parts = message.split(Patterns.SPACE);
            if (parts.length == 4 && parts[1].equals("ACC")) {
                if (parts[2].equals("3")) {
                    Auth.onUserVerified(bot.getConnection().createUser(parts[0]));
                }
            }
        }
    }

    @Override
    public void onPrivateMessage(IrcConnection irc, User sender, String message) {}
}
