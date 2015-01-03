package net.acomputerdog.ircbot.irc;

import com.sorcix.sirc.*;
import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.main.Channels;
import net.acomputerdog.ircbot.main.IrcBot;

public class IrcListener implements MessageListener, ServerListener {

    private static final CLogger LOGGER = new CLogger("IrcListener", false, true);

    private final IrcBot bot;

    private Channel activeChannel;

    public IrcListener(IrcBot ircBot) {
        this.bot = ircBot;
    }

   /*
   On channel action (/me)
    */
    @Override
    public void onAction(IrcConnection irc, User sender, Channel target, String action) {

    }

    /*
    On private action  (/me)
     */
    @Override
    public void onAction(IrcConnection irc, User sender, String action) {

    }

    @Override
    public void onCtcpReply(IrcConnection irc, User sender, String command, String message) {

    }

    @Override
    public void onMessage(IrcConnection irc, User sender, Channel target, String message) {
        Command.onChat(bot, target, sender, target, message);
    }

    @Override
    public void onNotice(IrcConnection irc, User sender, Channel target, String message) {

    }

    @Override
    public void onNotice(IrcConnection irc, User sender, String message) {

    }

    @Override
    public void onPrivateMessage(IrcConnection irc, User sender, String message) {
        Command.onChat(bot, null, sender, sender, message);
    }

    @Override
    public void onConnect(IrcConnection irc) {

    }

    @Override
    public void onDisconnect(IrcConnection irc) {
        LOGGER.logInfo("Disconnected from server.");
        bot.stop();
    }

    @Override
    public void onInvite(IrcConnection irc, User sender, User user, Channel channel) {

    }

    @Override
    public void onJoin(IrcConnection irc, Channel channel, User user) {
        if (user.isUs()) {
            LOGGER.logInfo("Joining channel " + channel.getName() + ".");
            Channels.connect(channel);
        }
    }

    @Override
    public void onKick(IrcConnection irc, Channel channel, User sender, User user, String msg) {
        if (user.isUs()) {
            LOGGER.logWarning("Kicked from channel " + channel.getName() + " by " + sender.getNick() + "/" + sender.getRealName() + "/" + sender.getHostName() + "!");
            Channels.disconnect(channel.getName());
        }
    }

    @Override
    public void onMode(IrcConnection irc, Channel channel, User sender, String mode) {

    }

    @Override
    public void onMotd(IrcConnection irc, String motd) {

    }

    @Override
    public void onNick(IrcConnection irc, User oldUser, User newUser) {

    }

    @Override
    public void onPart(IrcConnection irc, Channel channel, User user, String message) {
        if (user.isUs() && channel != null) {
            LOGGER.logInfo("Leaving channel " + channel.getName() + ".");
            Channels.disconnect(channel.getName());
        }
    }

    @Override
    public void onQuit(IrcConnection irc, User user, String message) {

    }

    @Override
    public void onTopic(IrcConnection irc, Channel channel, User sender, String topic) {
        channel.setTopic(topic);
    }
}
