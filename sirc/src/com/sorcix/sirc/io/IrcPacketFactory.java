package com.sorcix.sirc.io;

public class IrcPacketFactory {

    public static IrcPacket createAWAY(String reason) {
        return new IrcPacket(null, "AWAY", null, reason);
    }

    public static IrcPacket createMOTD() {
        return new IrcPacket(null, "MOTD", null, null);
    }

    public static IrcPacket createNAMES(String channel) {
        return new IrcPacket(null, "NAMES", channel, null);
    }

    public static IrcPacket createNICK(String nick) {
        return new IrcPacket(null, "NICK", nick, null);
    }

    public static IrcPacket createPASS(String password) {
        return new IrcPacket(null, "PASS", password, null);
    }

    public static IrcPacket createQUIT(String message) {
        return new IrcPacket(null, "QUIT", null, message);
    }

    public static IrcPacket createUSER(String username,
                                       String realname) {
        return new IrcPacket(null, "USER", username + " Sorcix.com *", realname);
    }

}
