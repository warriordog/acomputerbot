package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.*;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

import java.util.HashMap;
import java.util.Map;

public class CommandSpy extends Command {
    private final Map<User, Spy> spyMap = new HashMap<>();

    public CommandSpy(IrcBot bot) {
        super(bot, "Spy", "spy", "watch", "monitor", "listen");
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "Monitors conversation by a particular person.";
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "spy <target>";
    }

    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        if (command.args.equals("*")) {
            spyMap.keySet().forEach(u -> spyMap.get(u).stop());
            target.send("All spy sessions ended.");
            return true;
        } else {
            User user = bot.getConnection().createUser(command.args.toLowerCase());
            Spy spy = spyMap.get(user);
            if (spy != null) {
                spy.stop();
                spyMap.remove(user);
            } else {
                spy = new Spy(bot, user, target);
                spyMap.put(user, spy);
                spy.start();
            }
            return true;
        }
    }

    private static class Spy implements MessageListener {

        private final IrcBot bot;
        private final User spyTarget;
        private final Chattable spySender;
        private final String name;
        private final String targetName;

        private Spy(IrcBot bot, User spyTarget, Chattable spySender) {
            this.bot = bot;
            this.spyTarget = spyTarget;
            this.spySender = spySender;
            String targetNick = spyTarget.getNick();
            this.targetName = targetNick.substring(0, targetNick.length() - 1).concat("_");
            this.name = "[SPY][" + targetName + "]";
        }

        @Override
        public void onMessage(IrcConnection irc, User sender, Channel target, String message) {
            if (sender.equals(spyTarget)) {
                spySender.send(name + "[CHAT][" + target.getName() + "] " + message);
            }
        }

        private void start() {
            spySender.send(name + "[INFO] Spying started.");
            bot.getConnection().addMessageListener(this);
        }

        private void stop() {
            bot.getConnection().removeMessageListener(this);
            spySender.send(name + "[INFO] Spying stopped.");
        }

        @Override
        public void onAction(IrcConnection irc, User sender, Channel target, String action) {}
        @Override
        public void onAction(IrcConnection irc, User sender, String action) {}
        @Override
        public void onCtcpReply(IrcConnection irc, User sender, String command, String message) {}
        @Override
        public void onNotice(IrcConnection irc, User sender, Channel target, String message) {}
        @Override
        public void onNotice(IrcConnection irc, User sender, String message) {}
        @Override
        public void onPrivateMessage(IrcConnection irc, User sender, String message) {}
    }
}
