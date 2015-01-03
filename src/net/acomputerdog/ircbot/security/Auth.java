package net.acomputerdog.ircbot.security;

import com.sorcix.sirc.User;
import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.config.Admins;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

import java.util.HashMap;
import java.util.Map;

public class Auth {
    private final IrcBot bot;
    
    private final CLogger LOGGER = new CLogger("Auth", false, true);

    private final Map<User, Long> authenticatedAdmins = new HashMap<>();
    private final Map<User, Integer> loginAttempts = new HashMap<>();
    private final Map<User, Long> authFailedTimeouts = new HashMap<>();

    private final Map<User, Long> reauthWaitingAdmins = new HashMap<>();
    private final Map<User, String> verifyWaitingPass = new HashMap<>();
    private final Map<User, Long> verifyWaitingTimeout = new HashMap<>();

    public Auth(IrcBot bot) {
        this.bot = bot;
    }

    public void requestAuthentication(User user, String pass) {
        verifyWaitingPass.put(user, pass);
        verifyWaitingTimeout.put(user, System.currentTimeMillis() + 60000);
        IrcBot.instance.getNickServ().send("ACC " + user.getNick());
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
                    user.send("You have now been logged in!  Remember that if you log out for more than 10 minutes your session will expire!");
                    authenticatedAdmins.put(user, System.currentTimeMillis() + Config.AUTH_TIMEOUT);
                    loginAttempts.put(user, 0);
                    authFailedTimeouts.remove(user);
                    return true;
                } else {
                    user.send("Incorrect password, please try again!");
                    LOGGER.logWarning("User " + getUserName(user) + " failed authentication!");
                    LOGGER.logWarning("Reason: Incorrect password.");
                    LOGGER.logWarning("Password used: \"" + pass + "\".");
                }
            } else {
                user.send("You are not an AcomputerBot admin!");
                LOGGER.logWarning("User " + getUserName(user) + " failed authentication!");
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
                reauthWaitingAdmins.put(user, System.currentTimeMillis() + 60000);
            }
        }
        for (User user : reauthWaitingAdmins.keySet()) {
            if (reauthWaitingAdmins.get(user) <= System.currentTimeMillis()) {
                deauthenticate(user);
                user.send("Your AcomputerBot session has expired, you must reauthenticate to perform admin commands!");
            }
        }
        for (User user : verifyWaitingTimeout.keySet()) {
            if (verifyWaitingTimeout.get(user) <= System.currentTimeMillis()) {
                verifyWaitingTimeout.remove(user);
                verifyWaitingPass.remove(user);
                user.send("AcomputerBot was unable to verify your login status with NickServ!  This is a bug, please try logging in again!");
            }
        }
    }

    public boolean deauthenticate(User user) {
        if (!authenticatedAdmins.containsKey(user)) {
            return false;
        }
        authenticatedAdmins.remove(user);
        loginAttempts.remove(user);
        authFailedTimeouts.remove(user);
        reauthWaitingAdmins.remove(user);
        verifyWaitingTimeout.remove(user);
        verifyWaitingPass.remove(user);
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
}
