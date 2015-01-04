package net.acomputerdog.ircbot.config;

import com.sorcix.sirc.Channel;
import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.main.IrcBot;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class AutoJoinList {
    private final CLogger LOGGER = new CLogger("AutoJoinList", false, true);
    private final Set<String> autoJoinChannels = new HashSet<>();
    private final File saveFile = new File("./autojoin.txt");

    private final IrcBot bot;

    private int hash = -1;

    public AutoJoinList(IrcBot bot) {
        this.bot = bot;
    }

    public void addAutoJoinChannel(String channel) {
        autoJoinChannels.add(channel.toLowerCase());
    }

    public void addAutoJoinChannel(Channel channel) {
        addAutoJoinChannel(channel.getName());
    }

    public boolean isAutoJoined(String channel) {
        return autoJoinChannels.contains(channel.toLowerCase());
    }

    public boolean isAutoJoined(Channel channel) {
        return isAutoJoined(channel.getName());
    }

    public void joinChannels() {
        for (String channel : autoJoinChannels) {
            bot.getConnection().createChannel(channel).join();
        }
    }

    public void load() {
        try {
            if (saveFile.isFile()) {
                BufferedReader reader = new BufferedReader(new FileReader(saveFile));
                reader.lines().forEach(autoJoinChannels::add);
                reader.close();
                LOGGER.logInfo("Loaded auto-join list.");
            } else {
                LOGGER.logInfo("Auto-join list does not exist.  It will be created.");
                save();
            }
        } catch (Exception e) {
            LOGGER.logWarning("Exception loading auto-join list!", e);
        }
        hash = autoJoinChannels.hashCode();
    }

    public void save() {
        try {
            int currHash = autoJoinChannels.hashCode();
            if (hash != currHash) {
                Writer writer = new FileWriter(saveFile);
                int count = 0;
                for (String str : autoJoinChannels) {
                    if (count > 0) {
                        writer.write("\n");
                    }
                    writer.write(str);
                    count++;
                }
                writer.close();
                hash = currHash;
                LOGGER.logInfo("Saved auto-join list.");
            }
        } catch (Exception e) {
            LOGGER.logWarning("Exception saving auto-join list!", e);
        }
    }
}
