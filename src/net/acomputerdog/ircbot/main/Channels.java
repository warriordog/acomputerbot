package net.acomputerdog.ircbot.main;

import com.sorcix.sirc.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Channels {
    private static final Map<String, Channel> connectedChannels = new HashMap<>();

    public static void connect(Channel channel) {
        connectedChannels.put(getChannelName(channel.getName()), channel);
    }

    public static boolean isConnected(Channel channel) {
        return isConnected(channel.getName());
    }

    public static boolean isConnected(String channel) {
        return connectedChannels.containsKey(getChannelName(channel));
    }

    public static void disconnect(String channel) {
        connectedChannels.remove(getChannelName(channel));
    }

    public static Set<String> getChannels() {
        return connectedChannels.keySet();
    }

    public static void disconnectAll() {
        connectedChannels.values().forEach(Channel::part);
    }

    public static Channel getChannel(String channel) {
        return connectedChannels.get(getChannelName(channel));
    }

    private static String getChannelName(String name) {
        if (!name.isEmpty() && name.charAt(0) != '#') {
            return "#".concat(name.toLowerCase());
        }
        return name.toLowerCase();
    }
}
