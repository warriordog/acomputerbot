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

    public static String ADMIN_PASS = "compbot";

    public static int MAX_AUTH_ATTEMPTS = 5;

    public static long LOGIN_ATTEMPT_TIMEOUT = 1000 * 60 * 10; //10 minutes

    public static long AUTH_TIMEOUT = 1000 * 60 * 10; //10 minutes

    public static int MESSAGES_PER_SECOND = 2; //default is 10

    public static int MAX_CASCADED_COMMANDS = 2; //max number of "~" that can be in a chat said by AcomputerBot

    public static int CHAT_FILTER_MODE = 1; //0 is no filter, 1 is remove cascaded commands, 2 is block cascaded commands

    public static boolean ENABLE_BLACKLIST = true;

    public static boolean ENABLE_WHITELIST = false;

    public static long JS_TIMEOUT = 10000;

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
                ADMIN_PASS = prop.getProperty("ADMIN_PASS", ADMIN_PASS);
                MAX_AUTH_ATTEMPTS = prop.containsKey("MAX_AUTH_ATTEMPTS") ? Integer.parseInt(prop.getProperty("MAX_AUTH_ATTEMPTS")) : MAX_AUTH_ATTEMPTS;
                LOGIN_ATTEMPT_TIMEOUT = prop.containsKey("LOGIN_ATTEMPT_TIMEOUT") ? Long.parseLong(prop.getProperty("LOGIN_ATTEMPT_TIMEOUT")) : LOGIN_ATTEMPT_TIMEOUT;
                AUTH_TIMEOUT = prop.containsKey("AUTH_TIMEOUT") ? Long.parseLong(prop.getProperty("AUTH_TIMEOUT")) : AUTH_TIMEOUT;
                MESSAGES_PER_SECOND = prop.containsKey("MESSAGES_PER_SECOND") ? Integer.parseInt(prop.getProperty("MESSAGES_PER_SECOND")) : MESSAGES_PER_SECOND;
                MAX_CASCADED_COMMANDS = prop.containsKey("MAX_CASCADED_COMMANDS") ? Integer.parseInt(prop.getProperty("MAX_CASCADED_COMMANDS")) : MAX_CASCADED_COMMANDS;
                CHAT_FILTER_MODE = prop.containsKey("CHAT_FILTER_MODE") ? Integer.parseInt(prop.getProperty("CHAT_FILTER_MODE")) : CHAT_FILTER_MODE;
                ENABLE_BLACKLIST = prop.containsKey("ENABLE_BLACKLIST") ? Boolean.parseBoolean(prop.getProperty("ENABLE_BLACKLIST")) : ENABLE_BLACKLIST;
                ENABLE_WHITELIST = prop.containsKey("ENABLE_WHITELIST") ? Boolean.parseBoolean(prop.getProperty("ENABLE_WHITELIST")) : ENABLE_WHITELIST;
                JS_TIMEOUT = prop.containsKey("JS_TIMEOUT") ? Long.parseLong(prop.getProperty("JS_TIMEOUT")) : JS_TIMEOUT;
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
                prop.setProperty("BOT_NICK", BOT_NICK);
                prop.setProperty("BOT_PASS", BOT_PASS);
                prop.setProperty("SERVER", SERVER);
                prop.setProperty("COMMAND_PREFIX", COMMAND_PREFIX);
                prop.setProperty("MEMORY_BUFFER_SIZE", String.valueOf(MEMORY_BUFFER_SIZE));
                prop.setProperty("ADMIN_PASS", ADMIN_PASS);
                prop.setProperty("MAX_AUTH_ATTEMPTS", String.valueOf(MAX_AUTH_ATTEMPTS));
                prop.setProperty("LOGIN_ATTEMPT_TIMEOUT", String.valueOf(LOGIN_ATTEMPT_TIMEOUT));
                prop.setProperty("AUTH_TIMEOUT", String.valueOf(AUTH_TIMEOUT));
                prop.setProperty("MESSAGES_PER_SECOND", String.valueOf(MESSAGES_PER_SECOND));
                prop.setProperty("MAX_CASCADED_COMMANDS", String.valueOf(MAX_CASCADED_COMMANDS));
                prop.setProperty("CHAT_FILTER_MODE", String.valueOf(CHAT_FILTER_MODE));
                prop.setProperty("ENABLE_BLACKLIST", String.valueOf(ENABLE_BLACKLIST));
                prop.setProperty("ENABLE_WHITELIST", String.valueOf(ENABLE_WHITELIST));
                prop.setProperty("JS_TIMEOUT", String.valueOf(JS_TIMEOUT));
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
