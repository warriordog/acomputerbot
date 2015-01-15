package net.acomputerdog.ircbot.security;

import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

import java.util.HashMap;
import java.util.Map;

public class Auth {
    private final IrcBot bot;

    private final CLogger LOGGER;

    private final Map<User, Long> authenticatedAdmins = new HashMap<>();
    private final Map<User, Integer> loginAttempts = new HashMap<>();
    private final Map<User, Long> authFailedTimeouts = new HashMap<>();

    private final Map<User, Long> reauthWaitingAdmins = new HashMap<>();

    private final Map<User, String> verifyWaitingPass = new HashMap<>();
    private final Map<User, Long> verifyWaitingTimeout = new HashMap<>();
    private final Map<User, Chattable> loginReplyTarget = new HashMap<>();

    public Auth(IrcBot bot) {
        this.bot = bot;
        LOGGER = bot.getLogManager().getLogger("Auth");
    }

    public void requestAuthentication(User user, Chattable target, String pass) {
        verifyWaitingPass.put(user, pass);
        verifyWaitingTimeout.put(user, System.currentTimeMillis() + 60000);
        loginReplyTarget.put(user, target);
        bot.getNickServ().send("ACC " + user.getNick());
    }

    private boolean authenticate(User user, String pass) {
        if (loginAttempts.get(user) == null) {
            loginAttempts.put(user, 0);
        }
        int attempts = loginAttempts.get(user) + 1;
        loginAttempts.put(user, attempts);
        if (attempts < Config.MAX_AUTH_ATTEMPTS) {
            if (bot.getAdmins().isAdmin(user)) {
                if (pass.equals(Config.ADMIN_PASS)) {
                    LOGGER.logInfo("Admin " + getUserName(user) + " has been authenticated.");
                    loginReplyTarget.get(user).send("You have now been logged in!  Remember that if you log out for more than " + (Config.AUTH_TIMEOUT / 60000) + " minutes your session will expire!");
                    authenticatedAdmins.put(user, System.currentTimeMillis() + Config.AUTH_TIMEOUT);
                    loginAttempts.put(user, 0);
                    authFailedTimeouts.remove(user);
                    return true;
                } else {
                    loginReplyTarget.get(user).send("Incorrect password, please try again!");
                    logFailedAuth(user, "Incorrect password.", pass);
                }
            } else {
                loginReplyTarget.get(user).send("You are not an AcomputerBot admin!");
                logFailedAuth(user, "Not an admin.", pass);
            }
        } else {
            loginReplyTarget.get(user).send("You have too many failed login attempts!  Please try again in " + (Config.LOGIN_ATTEMPT_TIMEOUT / 60000) + " minutes.");
            authFailedTimeouts.put(user, System.currentTimeMillis() + Config.LOGIN_ATTEMPT_TIMEOUT);
            logFailedAuth(user, "Too many login attempts.", pass);
        }
        return false;
    }

    private void logFailedAuth(User user, String reason, String pass) {
        LOGGER.logWarning("User " + getUserName(user) + " failed authentication!");
        LOGGER.logWarning(" Reason: " + reason);
        LOGGER.logWarning(" Password used: \"" + pass + "\".");
    }

    private String getUserName(User user) {
        return user.getNick() + "@" + user.getHostName();
    }

    public boolean isAuthenticated(User user) {
        return authenticatedAdmins.containsKey(user);
    }

    public void tick() {
        for (User user : authFailedTimeouts.keySet()) {
            long time = authFailedTimeouts.get(user);
            if (time <= System.currentTimeMillis()) {
                authFailedTimeouts.remove(user);
                loginAttempts.put(user, 0);
            }
        }
        for (User user : authenticatedAdmins.keySet()) {
            if (authenticatedAdmins.get(user) <= System.currentTimeMillis()) {
                bot.getNickServ().send("ACC " + user.getNick());
                reauthWaitingAdmins.put(user, System.currentTimeMillis() + 60000);
            }
        }
        for (User user : reauthWaitingAdmins.keySet()) {
            if (reauthWaitingAdmins.get(user) <= System.currentTimeMillis()) {
                deauthenticate(user);
                user.send("Your admin session has expired, you must reauthenticate to perform admin commands!");
                LOGGER.logWarning("Authentication expired for admin " + user.getNick() + ".");
            }
        }
        for (User user : verifyWaitingTimeout.keySet()) {
            if (verifyWaitingTimeout.get(user) <= System.currentTimeMillis()) {
                verifyWaitingTimeout.remove(user);
                verifyWaitingPass.remove(user);
                loginReplyTarget.get(user).send("AcomputerBot was unable to verify your login status with NickServ!  This is a bug, please try logging in again!");
                LOGGER.logWarning("Unable to verify NickServ status for \"" + user.getNick() + "\"!");
            }
        }
    }

    public boolean deauthenticate(User user) {
        loginAttempts.remove(user);
        authFailedTimeouts.remove(user);
        reauthWaitingAdmins.remove(user);
        verifyWaitingTimeout.remove(user);
        verifyWaitingPass.remove(user);
        loginReplyTarget.remove(user);
        if (!authenticatedAdmins.containsKey(user)) {
            return false;
        }
        authenticatedAdmins.remove(user);
        LOGGER.logInfo("Admin " + getUserName(user) + " has been deauthenticated.");
        return true;
    }

    void onUserVerified(User user) {
        if (verifyWaitingPass.containsKey(user)) {
            authenticate(user, verifyWaitingPass.get(user));
            verifyWaitingPass.remove(user);
            verifyWaitingTimeout.remove(user);
        }
        if (authenticatedAdmins.containsKey(user)) {
            authenticatedAdmins.put(user, authenticatedAdmins.get(user) + Config.AUTH_TIMEOUT);
            reauthWaitingAdmins.remove(user);
            loginAttempts.put(user, 0);
            authFailedTimeouts.remove(user);
        }
    }

    void onUserUnverified(User user) {
        if (verifyWaitingPass.containsKey(user)) {
            logFailedAuth(user, "Not logged in to nickserv.", verifyWaitingPass.get(user));
            user.send("You must be logged into NickServ to log in as an AcomputerBot admin!");
        }
        if (reauthWaitingAdmins.containsKey(user)) {
            user.send("You have logged out of NickServ, and your admin session has expired!  You must log in again to perform admin commands.");
            LOGGER.logWarning("Admin " + user.getNick() + " has logged out of NickServ and lost authentication.");
        }
        deauthenticate(user);
    }
}
