package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.*;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

import java.util.HashMap;
import java.util.Map;

public class CommandSpyIn extends Command {
    private final Map<Channel, ChannelSpy> spyMap = new HashMap<>();

    public CommandSpyIn(IrcBot bot) {
        super(bot, "SpyIn", "spyin", "spy-in", "spy_in");
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "Monitors conversation in a channel.";
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "spyin <target>";
    }

    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        if (command.args.equals("*")) {
            spyMap.keySet().forEach(u -> spyMap.get(u).stop());
            target.send("All spy sessions ended.");
            return true;
        } else {
            String chanName = command.args.toLowerCase();
            Channel chan = bot.getConnection().createChannel(chanName);
            Map<String, Channel> channelMap = bot.getConnection().getState().getChannelMap();
            if (!channelMap.containsKey(chanName)) {
                chan.join();
            }
            ChannelSpy spy = spyMap.get(chan);
            if (spy != null) {
                spy.stop();
                spyMap.remove(chan);
            } else {
                spy = new ChannelSpy(bot, chan, target);
                spyMap.put(chan, spy);
                spy.start();
            }
            return true;
        }
    }

    private static class ChannelSpy implements MessageListener {

        private final IrcBot bot;
        private final Channel spyTarget;
        private final Chattable spySender;
        private final String name;
        private final String targetName;

        private ChannelSpy(IrcBot bot, Channel spyTarget, Chattable spySender) {
            this.bot = bot;
            this.spyTarget = spyTarget;
            this.spySender = spySender;
            this.targetName = formatName(spyTarget.getName());
            this.name = "[SPY][" + targetName + "]";
        }

        @Override
        public void onMessage(IrcConnection irc, User sender, Channel target, String message) {
            if (target.equals(spyTarget)) {
                spySender.send(name + "[CHAT][" + formatName(sender.getNick()) + "] " + message);
            }
        }

        private String formatName(String name) {
            return name.substring(0, name.length() - 1).concat("□");
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
