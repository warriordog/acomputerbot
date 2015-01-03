package net.acomputerdog.ircbot.config;

import com.sorcix.sirc.User;
import net.acomputerdog.core.logger.CLogger;

import java.io.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Admins {
    private static final CLogger LOGGER = new CLogger("AdminList", false, true);
    private static final Set<String> adminNames = new HashSet<>();
    private static final File saveFile = new File("./admins.cfg");

    private static int hash = -1;

    public static boolean isAdmin(String user) {
        return user != null && !user.isEmpty() && adminNames.contains(user);
    }

    public static boolean isAdmin(User user) {
        return user != null && isAdmin(user.getRealName());
    }

    public static void addAdmin(String user) {
        if (user != null && !user.isEmpty()) {
            adminNames.add(user);
        }
    }

    public static void addAdmin(User user) {
        if (user != null) {
            addAdmin(user.getRealName());
        }
    }

    public static void load() {
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

    public static void save() {
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
            LOGGER.logWarning("Exception loading admin list!", e);
        }
    }
}
