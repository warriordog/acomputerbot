/*
 * ClientState.java
 * 
 * This file is part of the Sorcix Java IRC Library (sIRC).
 * 
 * Copyright (C) 2008-2010 Vic Demuzere http://sorcix.com
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.sorcix.sirc.main;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains information about an {@link IrcConnection}.
 *
 * @author Sorcix
 * @since 1.1.0
 */
public class ClientState {

    // TODO: Allow changing the username (sIRC@..)
    /**
     * The list of channels.
     */
    private final Map<String, Channel> channels;
    /**
     * Contains a singleton for all known users.
     */
    private final Map<String, User> users;
    /**
     * The local user.
     */
    private User client;

    /**
     * Creates a new ClientState.
     */
    public ClientState() {
        this.channels = new HashMap<>();
        this.users = new HashMap<>();
    }

    /**
     * Adds a channel to the channel map.
     *
     * @param channel The channel to add.
     */
    public void addChannel(Channel channel) {
        this.channels.put(channel.getName().toLowerCase(), channel);
    }

    /**
     * Adds a user to the user map.
     *
     * @param user The user to add.
     */
    public void addUser(User user) {
        this.users.put(user.getNickLower(), user);
    }

    /**
     * Retrieves a shared channel object from the channel map.
     *
     * @param channel A channel object representing this channel.
     * @return The channel, or null if this channel doesn't exist. (The local
     * user is not in that channel)
     * @see #getChannel(String)
     */
    public Channel getChannel(Channel channel) {
        return this.getChannel(channel.getName());
    }

    /**
     * Retrieves a shared channel object from the channel map.
     *
     * @param channel The channel name.
     * @return The channel, or null if this channel doesn't exist. (The local
     * user is not in that channel)
     */
    public Channel getChannel(String channel) {
        if (channel != null && this.channels.containsKey(channel.toLowerCase())) {
            return this.channels.get(channel.toLowerCase());
        }
        return null;
    }

    public Map<String, Channel> getChannelMap() {
        return Collections.unmodifiableMap(channels);
    }

    /**
     * Retrieves the local {@link User}.
     *
     * @return The local {@code User}.
     */
    public User getClient() {
        return this.client;
    }

    /**
     * Set the local {@link User}.
     *
     * @param user The local {@code User}.
     */
    public void setClient(User user) {
        this.client = user;
    }

    /**
     * Retrieves a shared user object from the users map.
     *
     * @param nick The nickname of this user.
     * @return The shared user object, or null if there is no singleton User
     * object for this user.
     */
    public User getUser(String nick) {
        //TODO: implement singleton users in User, Channel and IrcConnection
        if (this.users.containsKey(nick)) {
            return this.users.get(nick);
        }
        return null;
    }

    /**
     * Checks if given channel is in the channel map.
     *
     * @param name The name of this channel.
     * @return True if the channel is in the list, false otherwise.
     */
    public boolean hasChannel(String name) {
        return name != null && this.channels.containsKey(name.toLowerCase());
    }

    /**
     * Remove all channels from the channel map.
     */
    public void removeAll() {
        this.channels.clear();
    }

    /**
     * Removes a channel from the channel map.
     *
     * @param channel The channel name.
     */
    public void removeChannel(String channel) {
        if (channel != null) {
            this.channels.remove(channel.toLowerCase());
        }
    }
}
