package net.acomputerdog.ircbot.config;

import net.acomputerdog.core.hash.Hasher;
import net.acomputerdog.core.logger.CLogger;

import java.io.*;
import java.util.Properties;

public class Config {

    public static int TPS = 20;

    public static boolean USE_LOGIN = false;

    public static String BOT_USERNAME = "compbot_test";

    public static String BOT_NICK = "compbot_test";

    public static String BOT_PASS = "";

    public static String SERVER = "irc.esper.net";

    public static String COMMAND_PREFIX = "!";

    public static int MEMORY_BUFFER_SIZE = 1000000;

    //---------Internal Stuff--------------

    private static final CLogger LOGGER = new CLogger("Config", false, true);
    private static final File saveFile = new File("./config.cfg");
    private static int hash = 0;

    public static void load() {
        try {
            if (saveFile.isFile()) {
                Properties prop = new Properties();
                InputStream in = new FileInputStream(saveFile);
                prop.load(in);
                in.close();
                TPS = prop.containsKey("TPS") ? Integer.parseInt(prop.getProperty("TPS")) : TPS;
                USE_LOGIN = prop.containsKey("USE_LOGIN") ? Boolean.parseBoolean(prop.getProperty("USE_LOGIN")) : USE_LOGIN;
                BOT_USERNAME = prop.getProperty("BOT_USERNAME", BOT_USERNAME);
                BOT_NICK = prop.getProperty("BOT_NICK", BOT_NICK);
                BOT_PASS = prop.getProperty("BOT_PASS", BOT_PASS);
                SERVER = prop.getProperty("SERVER", SERVER);
                COMMAND_PREFIX = prop.getProperty("COMMAND_PREFIX", COMMAND_PREFIX);
                MEMORY_BUFFER_SIZE = prop.containsKey("MEMORY_BUFFER_SIZE") ? Integer.parseInt(prop.getProperty("MEMORY_BUFFER_SIZE")) : MEMORY_BUFFER_SIZE;
            } else {
                LOGGER.logInfo("Configuration file does not exist.  It will be created.");
                save();
            }
            LOGGER.logInfo("Configuration loaded.");
        } catch (Exception e) {
            LOGGER.logError("Unable to load configuration!", e);
        }
        hash = calculateHash();
    }

    public static void save() {
        int currHash = calculateHash();
        try {
            if (currHash != hash) {
                Properties prop = new Properties();
                prop.setProperty("TPS", String.valueOf(TPS));
                prop.setProperty("USE_LOGIN", String.valueOf(USE_LOGIN));
                prop.setProperty("BOT_USERNAME", BOT_USERNAME);
                prop.setProperty("BOT_NICK", String.valueOf(BOT_NICK));
                prop.setProperty("BOT_PASS", String.valueOf(BOT_PASS));
                prop.setProperty("SERVER", String.valueOf(SERVER));
                prop.setProperty("COMMAND_PREFIX", String.valueOf(COMMAND_PREFIX));
                prop.setProperty("MEMORY_BUFFER_SIZE", String.valueOf(MEMORY_BUFFER_SIZE));
                OutputStream out = new FileOutputStream(saveFile);
                prop.store(out, "AcomputerBot configuration file.  Lines prefixed with '#' will be ignored.");
                out.close();
                hash = currHash;
                LOGGER.logInfo("Configuration saved.");
            }
        } catch (Exception e) {
            LOGGER.logError("Unable to save configuration!", e);
        }
    }

    private static int calculateHash() {
        return new Hasher().hash(TPS).hash(USE_LOGIN).hash(BOT_USERNAME).hash(BOT_NICK).hash(BOT_PASS).hash(SERVER).hash(COMMAND_PREFIX).hashCode();
    }
}
