package net.acomputerdog.ircbot.security;

import com.sorcix.sirc.User;
import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.config.Admins;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

import java.util.HashMap;
import java.util.Map;

public class Auth {
    private static final CLogger LOGGER = new CLogger("Auth", false, true);

    private static final Map<User, Long> authenticatedAdmins = new HashMap<>();
    private static final Map<User, Integer> loginAttempts = new HashMap<>();
    private static final Map<User, Long> authFailedTimeouts = new HashMap<>();

    private static final Map<User, Long> reauthWaitingAdmins = new HashMap<>();
    private static final Map<User, String> verifyWaitingPass = new HashMap<>();
    private static final Map<User, Long> verifyWaitingTimeout = new HashMap<>();

    public static void requestAuthentication(User user, String pass) {
        verifyWaitingPass.put(user, pass);
        verifyWaitingTimeout.put(user, System.currentTimeMillis() + 60000);
        IrcBot.instance.getNickservListener().getNickServ().send("ACC " + user.getNick());
        //IrcBot.instance.getConnection().sendRaw("/msg NickServ ACC " + user.getNick());
    }

    private static boolean authenticate(User user, String pass) {
        if (loginAttempts.get(user) == null) {
            loginAttempts.put(user, 0);
        }
        int attempts = loginAttempts.get(user) + 1;
        loginAttempts.put(user, attempts);
        if (attempts < Config.MAX_AUTH_ATTEMPTS) {
            if (Admins.isAdmin(user)) {
                if (pass.equals(Config.ADMIN_PASS)) {
                    user.send("You have now been logged in!  Remember that if you log out for more than 10 minutes your session will expire!");
                    authenticatedAdmins.put(user, System.currentTimeMillis() + Config.AUTH_TIMEOUT);
                    loginAttempts.put(user, 0);
                    authFailedTimeouts.remove(user);
                    return true;
                } else {
                    user.send("Incorrect password, please try again!");
                    LOGGER.logWarning("User " + user.getHostName() + " failed authentication!");
                    LOGGER.logWarning("Reason: Incorrect password.");
                    LOGGER.logWarning("Password used: \"" + pass + "\".");
                }
            } else {
                user.send("You are not an AcomputerBot admin!");
                LOGGER.logWarning("User " + user.getHostName() + " failed authentication!");
                LOGGER.logWarning("Reason: Not an admin.");
                LOGGER.logWarning("Password used: \"" + pass + "\".");
            }
        } else {
            user.send("You have too many failed login attempts!  Pleas try again in 10 minutes.");
            authFailedTimeouts.put(user, System.currentTimeMillis() + Config.LOGIN_ATTEMPT_TIMEOUT);
            LOGGER.logWarning("User " + user.getHostName() + " failed authentication!");
            LOGGER.logWarning("Reason: Too many login attempts.");
            LOGGER.logWarning("Password used: \"" + pass + "\".");
        }
        return false;
    }

    public static boolean isAuthenticated(User user) {
        return authenticatedAdmins.containsKey(user);
    }

    public static void tick() {
        for (User user : authFailedTimeouts.keySet()) {
            long time = authFailedTimeouts.get(user);
            if (time <= System.currentTimeMillis()) {
                authFailedTimeouts.remove(user);
                loginAttempts.put(user, 0);
            }
        }
        for (User user : authenticatedAdmins.keySet()) {
            if (authenticatedAdmins.get(user) <= System.currentTimeMillis()) {
                reauthWaitingAdmins.put(user, System.currentTimeMillis() + 60000);
            }
        }
        for (User user : reauthWaitingAdmins.keySet()) {
            if (reauthWaitingAdmins.get(user) <= System.currentTimeMillis()) {
                reauthWaitingAdmins.remove(user);
                authenticatedAdmins.remove(user);
                loginAttempts.remove(user);
                authFailedTimeouts.remove(user);
                user.send("Your AcomputerBot session has expired, you must reauthenticate to perform admin commands!");
            }
        }
        for (User user : verifyWaitingTimeout.keySet()) {
            if (verifyWaitingTimeout.get(user) <= System.currentTimeMillis()) {
                verifyWaitingTimeout.remove(user);
                verifyWaitingPass.remove(user);
                user.send("AcomputerBot was unable to verify your login status with NickServ!  Please try logging in again!");
            }
        }
    }

    static void onUserVerified(User user) {
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
}
