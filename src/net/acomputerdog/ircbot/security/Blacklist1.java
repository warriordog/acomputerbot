package net.acomputerdog.ircbot.security;

import com.sorcix.sirc.User;
import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class Blacklist1 {

    private final CLogger LOGGER = new CLogger("BlackList", false, true);
    private final Set<String> blacklistedUsers = new HashSet<>();
    private final Set<String> whitelistedUsers = new HashSet<>();
    private final File blacklistSaveFile = new File("./blacklist.txt");
    private final File whitelistSaveFile = new File("./whitelist.txt");

    private final IrcBot bot;

    private int whitelistHash = -1;
    private int blacklistHash = -1;

    public Blacklist1(IrcBot bot) {
        this.bot = bot;
    }

    public void addWhitelisted(String user) {
        whitelistedUsers.add(user);
    }

    public void addWhitelisted(User user) {
        addWhitelisted(user.getNick());
    }

    public void addBlacklisted(String user) {
        blacklistedUsers.add(user);
    }

    public void addBlacklisted(User user) {
        addBlacklisted(user.getNick());
    }

    public boolean isBlacklisted(User user) {
        return blacklistedUsers.contains(user.getNick());
    }

    public boolean isWhitelisted(User user) {
        return whitelistedUsers.contains(user.getNick());
    }

    public boolean canUseBot(User user) {
        if (!Config.ENABLE_BLACKLIST || bot.getAuth().isAuthenticated(user)) {
            return true;
        }
        if (isBlacklisted(user)) {
            return Config.ENABLE_WHITELIST && isWhitelisted(user);
        }
        return true;
    }

    public void load() {
        try {
            if (!blacklistSaveFile.exists() || !whitelistSaveFile.exists()) {
                LOGGER.logInfo("Blacklist or whitelist does not exist.  It will be created.");
                save();
            }
            BufferedReader blacklistReader = new BufferedReader(new FileReader(blacklistSaveFile));
            blacklistReader.lines().forEach(blacklistedUsers::add);
            blacklistReader.close();
            LOGGER.logInfo("Loaded blacklist.");
            BufferedReader whitelistReader = new BufferedReader(new FileReader(whitelistSaveFile));
            whitelistReader.lines().forEach(whitelistedUsers::add);
            whitelistReader.close();
            LOGGER.logInfo("Loaded whitelist.");
        } catch (Exception e) {
            LOGGER.logWarning("Exception loading blacklist!", e);
        }
        whitelistHash = whitelistedUsers.hashCode();
        blacklistHash = blacklistedUsers.hashCode();
    }

    public void save() {
        try {
            int currWhitelistHash = whitelistedUsers.hashCode();
            int currBlacklistHash = blacklistedUsers.hashCode();
            if (whitelistHash != currWhitelistHash) {
                Writer writer = new FileWriter(whitelistSaveFile);
                int count = 0;
                for (String str : whitelistedUsers) {
                    if (count > 0) {
                        writer.write("\n");
                    }
                    writer.write(str);
                    count++;
                }
                writer.close();
                whitelistHash = currWhitelistHash;
                LOGGER.logInfo("Saved whitelist.");
            }
            if (blacklistHash != currBlacklistHash) {
                Writer writer = new FileWriter(blacklistSaveFile);
                int count = 0;
                for (String str : blacklistedUsers) {
                    if (count > 0) {
                        writer.write("\n");
                    }
                    writer.write(str);
                    count++;
                }
                writer.close();
                blacklistHash = currBlacklistHash;
                LOGGER.logInfo("Saved blacklist.");
            }
        } catch (Exception e) {
            LOGGER.logWarning("Exception loading blacklist!", e);
        }
    }
}
