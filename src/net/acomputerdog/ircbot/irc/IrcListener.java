package net.acomputerdog.ircbot.irc;

import com.sorcix.sirc.io.IrcPacket;
import com.sorcix.sirc.listener.UnknownListener;
import com.sorcix.sirc.main.IrcConnection;
import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.IrcAdaptor;
import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

public class IrcListener extends IrcAdaptor implements UnknownListener {

    private final CLogger LOGGER;

    private final IrcBot bot;

    public IrcListener(IrcBot ircBot) {
        this.bot = ircBot;
        LOGGER = bot.getLogManager().getLogger("IRCListener");
    }

    @Override
    public void onMessage(IrcConnection irc, User sender, Channel target, String message) {
        if (message.toLowerCase().startsWith("*botinfo")) {
            if (target == null) {
                sender.send("BOTINFO: " + bot.getVersionString() + " (" + IrcConnection.ABOUT + ").  Type \"" + Config.COMMAND_PREFIX + "info\" for more information.");
            } else {
                target.send("BOTINFO: " + bot.getVersionString() + " (" + IrcConnection.ABOUT + ").  Type \"" + Config.COMMAND_PREFIX + "info\" for more information.");
            }
        } else {
            Command.onChat(bot, target, sender, target, message);
        }
    }

    @Override
    public void onPrivateMessage(IrcConnection irc, User sender, String message) {
        Command.onChat(bot, null, sender, sender, message);
    }

    @Override
    public void onDisconnect(IrcConnection irc) {
        LOGGER.logInfo("Disconnected from server.");
        bot.stop();
    }

    @Override
    public void onJoin(IrcConnection irc, Channel channel, User user) {
        if (user.isUs()) {
            LOGGER.logInfo("Joining channel " + channel.getName() + ".");
            bot.getConnection().sendRaw("WHO " + channel.getName());
        } else {
            bot.getConnection().sendRaw("WHO " + user.getName());
        }
    }

    @Override
    public void onKick(IrcConnection irc, Channel channel, User sender, User user, String msg) {
        if (user.isUs()) {
            LOGGER.logWarning("Kicked from channel " + channel.getName() + " by " + sender.getNick() + "/" + sender.getRealName() + "/" + sender.getHostName() + "!");
        }
    }

    @Override
    public void onPart(IrcConnection irc, Channel channel, User user, String message) {
        if (user.isUs() && channel != null) {
            LOGGER.logInfo("Leaving channel " + channel.getName() + ".");
        } else {
            bot.getAuth().deauthenticate(user);
        }
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
