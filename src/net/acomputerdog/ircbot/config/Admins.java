package net.acomputerdog.ircbot.config;

import com.sorcix.sirc.User;
import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.main.IrcBot;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Admins {
    private final IrcBot bot;

    private final CLogger LOGGER;
    private final Set<String> adminNames = new HashSet<>();
    private final File saveFile = new File("./admins.cfg");

    private int hash = -1;

    public Admins(IrcBot bot) {
        this.bot = bot;
        LOGGER = bot.getLogManager().getLogger("AdminList");
    }

    public boolean isAdmin(String user) {
        return user != null && !user.isEmpty() && adminNames.contains(user);
    }

    public boolean isAdmin(User user) {
        return user != null && isAdmin(user.getRealName());
    }

    public void addAdmin(String user) {
        if (user != null && !user.isEmpty()) {
            adminNames.add(user);
        }
    }

    public void addAdmin(User user) {
        if (user != null) {
            addAdmin(user.getRealName());
        }
    }

    public Set<String> getAdmins() {
        return Collections.unmodifiableSet(adminNames);
    }

    public void load() {
        try {
            if (saveFile.isFile()) {
                BufferedReader reader = new BufferedReader(new FileReader(saveFile));
                reader.lines().forEach(adminNames::add);
                reader.close();
                LOGGER.logInfo("Loaded admin list.");
            } else {
                LOGGER.logInfo("Admin list does not exist.  It will be created.");
                save();
            }
        } catch (Exception e) {
            LOGGER.logWarning("Exception loading admin list!", e);
        }
        hash = adminNames.hashCode();
    }

    public void save() {
        try {
            int currHash = adminNames.hashCode();
            if (hash != currHash) {
                Writer writer = new FileWriter(saveFile);
                int count = 0;
                for (String str : adminNames) {
                    if (count > 0) {
                        writer.write("\n");
                    }
                    writer.write(str);
                    count++;
                }
                writer.close();
                hash = currHash;
                LOGGER.logInfo("Saved admin list.");
            }
        } catch (Exception e) {
            LOGGER.logWarning("Exception saving admin list!", e);
        }
    }
}
