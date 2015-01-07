package net.acomputerdog.ircbot.irc;

import com.sorcix.sirc.io.IrcPacket;
import com.sorcix.sirc.listener.MessageListener;
import com.sorcix.sirc.listener.ServerListener;
import com.sorcix.sirc.listener.UnknownListener;
import com.sorcix.sirc.main.IrcConnection;
import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.main.IrcBot;

public class IrcListener implements MessageListener, ServerListener, UnknownListener {

    private final CLogger LOGGER;

    private final IrcBot bot;

    private Channel activeChannel;

    public IrcListener(IrcBot ircBot) {
        this.bot = ircBot;
        LOGGER = bot.getLogManager().getLogger("IRCListener");
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
            bot.getConnection().sendRaw("WHO " + channel.getName());
            //System.out.println("Sent WHO " + channel.getName());
            //for (String nick : channel.getUsers().keySet()) {
            //    bot.getConnection().sendRaw("/WHO " + nick);
            //}
        } else {
            bot.getConnection().sendRaw("WHO " + user.getName());
            //System.out.println("Sent WHO " + user.getName());
        }
        //bot.getConnection().sendRaw("/WHO " + user.getNick());
    }

    @Override
    public void onKick(IrcConnection irc, Channel channel, User sender, User user, String msg) {
        if (user.isUs()) {
            LOGGER.logWarning("Kicked from channel " + channel.getName() + " by " + sender.getNick() + "/" + sender.getRealName() + "/" + sender.getHostName() + "!");
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
        }
    }

    @Override
    public void onQuit(IrcConnection irc, User user, String message) {

    }

    @Override
    public void onTopic(IrcConnection irc, Channel channel, User sender, String topic) {
        channel.setTopic(topic);
    }

    @Override
    public void onUnknown(IrcConnection irc, IrcPacket line) {
        if ("352".equals(line.getCommand())) {
            String realname = line.getMessage().substring(2);
            String[] parts = line.getArgumentsArray();
            User user = irc.createUser(parts[5], parts[1]);
            user.setHostName(parts[3]);
            user.setRealName(realname);
            user.setUserName(parts[2]);
        }
    }

    @Override
    public void onUnknownReply(IrcConnection irc, IrcPacket line) {
    }
}
