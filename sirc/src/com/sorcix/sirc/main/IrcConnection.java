/*
 * IrcConnection.java
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

import com.sorcix.sirc.io.IrcInput;
import com.sorcix.sirc.io.IrcOutput;
import com.sorcix.sirc.io.IrcPacket;
import com.sorcix.sirc.io.IrcPacketFactory;
import com.sorcix.sirc.listener.*;
import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.IrcServer;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.NickNameException;
import com.sorcix.sirc.util.PasswordException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Main IRC Connection class in sIRC.
 * <p>
 * sIRC acts as a layer between an IRC server and java applications. It provides
 * an event-driven architecture to handle common IRC events.
 * </p>
 *
 * @author Sorcix
 */
public class IrcConnection {

    /**
     * sIRC Library version.
     */
    public static final String VERSION = "1.1.6-SNAPSHOT";
    /**
     * End line character.
     */
    public static final String ENDLINE = "\n";
    public static String ABOUT_ADDITIONAL = "";
    /**
     * The sIRC about string, used in CTCP
     */
    public static String ABOUT = "Sorcix Lib-IRC (sIRC) v" + IrcConnection.VERSION;
    /**
     * Message listeners.
     */
    private final Set<MessageListener> messageListeners;
    /**
     * Mode listeners.
     */
    private final Set<ModeListener> modeListeners;
    /**
     * Server listeners.
     */
    private final Set<ServerListener> serverListeners;
    /**
     * Services.
     */
    private final Set<SIRCService> services;
    /**
     * IRC Client state.
     */
    private final ClientState state;
    /**
     * Advanced listener.
     */
    private UnknownListener unknownListener = null;
    /**
     * Connection InputStream thread.
     */
    private IrcInput in = null;
    /**
     * Outgoing message delay. (Flood control)
     */
    private int messageDelay = 10; //changed to messages per second instead of time per message. (previously 100)
    private IrcOutput out = null;
    /**
     * Connection socket.
     */
    private Socket socket = null;
    /**
     * Custom version string.
     */
    private String version = null;
    /**
     * The server this IrcConnection is connected to.
     */
    private IrcServer server;
    /**
     * Whether we're connected or not.
     */
    private boolean connected;
    /**
     * The Character set to use for encoding the connection
     */
    private Charset charset = Charset.defaultCharset();
    /**
     * Whether to allow server redirection (bounce) or not.
     */
    private boolean bounceAllowed = false;

    /**
     * Creates a new IrcConnection object.
     */
    public IrcConnection() {
        this(null, IrcServer.DEFAULT_PORT, null);
    }

    /**
     * Creates a new IrcConnection object.
     *
     * @param server Server address.
     */
    public IrcConnection(String server) {
        this(server, IrcServer.DEFAULT_PORT, null);
    }

    /**
     * Creates a new IrcConnection object.
     *
     * @param server Server address.
     * @param port   Port number to connect to.
     */
    public IrcConnection(String server, int port) {
        this(server, port, null);
    }

    /**
     * Creates a new IrcConnection object.
     *
     * @param server   Server address.
     * @param port     Port number to connect to
     * @param password The password to use.
     */
    public IrcConnection(String server, int port,
                         String password) {
        this.server = new IrcServer(server, port, password, false);
        this.serverListeners = new CopyOnWriteArraySet<>();
        this.messageListeners = new CopyOnWriteArraySet<>();
        this.modeListeners = new CopyOnWriteArraySet<>();
        this.services = new CopyOnWriteArraySet<>();
        this.state = new ClientState();
    }

    /**
     * Creates a new IrcConnection object.
     *
     * @param server   Server address.
     * @param password The password to use.
     */
    public IrcConnection(String server, String password) {
        this(server, IrcServer.DEFAULT_PORT, password);
    }

    /**
     * Adds a message listener to this IrcConnection.
     *
     * @param listener The message listener to add.
     */
    public void addMessageListener(MessageListener listener) {
        if ((listener != null) && !this.messageListeners.contains(listener)) {
            this.messageListeners.add(listener);
        }
    }

    /**
     * Adds a mode listener to this IrcConnection. Note that adding mode
     * listeners will cause sIRC to check every incoming mode change for
     * supported modes. Modes can also be read by using
     * {@link ServerListener#onMode(IrcConnection, com.sorcix.sirc.structure.Channel, com.sorcix.sirc.structure.User, String)} which
     * can be a lot faster for reading modes.
     *
     * @param listener The mode listener to add.
     */
    public void addModeListener(ModeListener listener) {
        if ((listener != null) && !this.modeListeners.contains(listener)) {
            this.modeListeners.add(listener);
        }
    }

    /**
     * Adds a server listener to this IrcConnection.
     *
     * @param listener The server listener to add.
     */
    public void addServerListener(ServerListener listener) {
        if ((listener != null) && !this.serverListeners.contains(listener)) {
            this.serverListeners.add(listener);
        }
    }

    /**
     * Add and load a service. {@code IrcConnection} will call the
     * {@link SIRCService#load(IrcConnection)} method of this
     * {@code SIRCService} after adding it to the service list.
     *
     * @param service The service to add.
     */
    public void addService(SIRCService service) {
        if ((service != null) && !this.services.contains(service)) {
            this.services.add(service);
            service.load(this);
        }
    }

    /**
     * Sends the MOTD command to the server, which makes the server send us the
     * Message of the Day. (Through ServerListener)
     *
     * @see ServerListener#onMotd(IrcConnection, String)
     * @since 1.0.2
     */
    public void askMotd() {
        this.getOut().send(IrcPacketFactory.createMOTD());
    }

    /**
     * Send a raw command to the IRC server.  Unrecognized responses
     * are passed to the AdvancedListener's onUnknown() callback.
     *
     * @param line The raw line to send.
     */
    public void sendRaw(String line) {
        this.getOut().send(line);
    }

    /**
     * Asks the userlist for a certain channel.
     *
     * @param channel The channel to request the userlist for.
     */
    public void askNames(Channel channel) {
        this.getOut().send(IrcPacketFactory.createNAMES(channel.getName()));
    }

    /**
     * Closes all streams.
     */
    private void close() {
        try {
            this.in.interrupt();
            this.getOut().interrupt();
            // close input stream
            this.in.close();
            // close output stream
            this.getOut().close();
            // close socket
            if (this.socket.isConnected()) {
                this.socket.close();
            }
        } catch (Exception ex) {
            // ignore
        }
    }

    /**
     * Connect to the IRC server. You must set the server details and nickname
     * before calling this method!
     *
     * @throws UnknownHostException When the domain name is invalid.
     * @throws IOException          When anything went wrong while connecting.
     * @throws com.sorcix.sirc.util.NickNameException    If the given nickname is already in use or invalid.
     * @throws com.sorcix.sirc.util.PasswordException    If the server password is incorrect.
     * @see #setServer(String, int)
     * @see #setNick(String)
     */
    public void connect() throws IOException, NickNameException, PasswordException {
        this.connect((SSLContext) null);
    }

    /**
     * Connect to the IRC server. You must set the server details and nickname
     * before calling this method!
     *
     * @param sslctx The SSLContext to use.
     * @throws UnknownHostException When the domain name is invalid.
     * @throws IOException          When anything went wrong while connecting.
     * @throws NickNameException    If the given nickname is already in use or invalid.
     * @throws PasswordException    If the server password is incorrect.
     * @see #setServer(String, int)
     * @see #setNick(String)
     */
    public void connect(SSLContext sslctx) throws IOException, NickNameException, PasswordException {
        if (this.server.isSecure()) {
            try {
                if (sslctx == null)
                    sslctx = SSLContext.getDefault();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            this.connect(sslctx.getSocketFactory());
        } else {
            this.connect(SocketFactory.getDefault());
        }
    }

    /**
     * Connect to the IRC server. You must set the server details and nickname
     * before calling this method!
     *
     * @param sfact The SocketFactory to create a socket with.
     * @throws UnknownHostException When the domain name is invalid.
     * @throws IOException          When anything went wrong while connecting.
     * @throws NickNameException    If the given nickname is already in use or invalid.
     * @throws PasswordException    If the server password is incorrect.
     * @see #setServer(String, int)
     * @see #setNick(String)
     */
    public void connect(SocketFactory sfact) throws IOException, NickNameException, PasswordException {
        // check if a server is given
        if ((this.server.getAddress() == null)) {
            throw new IOException("Server address is not set!");
        }
        // connect socket
        if (this.socket == null || !this.socket.isConnected()) {
            Socket socket = sfact.createSocket(this.server.getAddress(), this.server.getPort());
            this.socket = null;
            this.connect(socket);
        } else {
            this.connect(this.socket);
        }
    }

    /**
     * Connect to the IRC server. You must set the server details and nickname
     * before calling this method!
     *
     * @param sock The socket to connect to.
     * @throws UnknownHostException When the domain name is invalid.
     * @throws IOException          When anything went wrong while connecting.
     * @throws NickNameException    If the given nickname is already in use or invalid.
     * @throws PasswordException    If the server password is incorrect.
     * @see #setServer(String, int)
     * @see #setNick(String)
     * @since 1.0.0
     */
    public void connect(Socket sock) throws IOException, NickNameException, PasswordException {
        boolean reconnecting = true;
        // don't even try if nickname is empty
        if ((this.state.getClient() == null) || this.state.getClient().getNick().trim().equals("")) {
            throw new NickNameException("Nickname is empty or null!");
        }
        // allows for handling SASL, etc. before doing IRC handshake
        // set to input socket
        if (sock != null && this.socket != sock) {
            this.socket = sock;
            reconnecting = false;
        }
        // open streams
        this.out = new IrcOutput(this, new OutputStreamWriter(this.socket.getOutputStream(), this.charset));
        this.in = new IrcInput(this, new InputStreamReader(this.socket.getInputStream(), this.charset));
        if (!reconnecting) {
            // send password if given
            if (this.server.getPassword() != null) {
                this.getOut().sendNowEx(IrcPacketFactory.createPASS(this.server
                        .getPassword()));
            }
            this.getOut().sendNowEx(IrcPacketFactory.createUSER(this.state.getClient()
                    .getUserName(), this.state.getClient().getNick()));
        }
        this.getOut().sendNowEx(IrcPacketFactory.createNICK(this.state.getClient()
                .getNick()));
        // wait for reply
        String line;
        loop:
        while ((line = this.in.getReader().readLine()) != null) {
            //IrcDebug.log(line);
            IrcPacket decoder = new IrcPacket(line, this);
            if (decoder.isNumeric()) {
                int command = decoder.getNumericCommand();
                switch (command) {
                    case 1:
                    case 2:
                    case 3: {
                        String nick = decoder.getArgumentsArray()[0];
                        if (!this.state.getClient().getNick().equals(nick))
                            this.setNick(nick);
                    }
                    break;
                    case 4: // login OK
                        break loop;
                    case 432:
                    case 433: {
                        // bad/in-use nickname nickname
                        throw new NickNameException("Nickname " + this.state.getClient().getNick() + " already in use or not allowed!");
                    } // break; unnecessary due to throw
                    case 464: {
                        // wrong password
                        this.disconnect();
                        throw new PasswordException("Invalid password");
                    } // break; unnecessary due to throw
                }
            }
            if (line.startsWith("PING ")) {
                this.getOut().pong(line.substring(5));
            }
        }
        // start listening
        this.in.start();
        this.getOut().start();
        // we are connected
        this.setConnected(true);
        // send events
        serverListeners.forEach(s -> s.onConnect(this));
    }

    /**
     * Creates a {@link Channel} object with given channel name. Note that this
     * method does not actually create a channel on the IRC server, it just
     * creates a {@link Channel} object linked to this {@code IrcConnection}. If
     * the local user is in the channel this method will return a global channel
     * object containing a user list.
     *
     * @param name The channel name, starting with #.
     * @return A {@code Channel} object representing given channel.
     * @see Channel#isGlobal()
     */
    public Channel createChannel(String name) {
        if (Channel.CHANNEL_PREFIX.indexOf(name.charAt(0)) < 0) {
            name = "#" + name;
        }
        if (this.getState().hasChannel(name)) {
            return this.getState().getChannel(name);
        } else {
            return new Channel(name, this, false);
        }
    }

    /**
     * Creates a {@link com.sorcix.sirc.structure.User} object with given nickname. This will create a
     * {@link com.sorcix.sirc.structure.User} object without any information about modes.
     *
     * @param nick The nickname.
     * @return A {@code User} object representing given user.
     * @see com.sorcix.sirc.structure.User#User(String, IrcConnection)
     */
    public User createUser(String nick) {
        return new User(nick, this);
    }

    /**
     * Creates a {@link User} object with given nickname. This will attempt to
     * retrieve a global {@link User} object for given {@link Channel}
     * containing information about user modes. If it isn't possible to return a
     * global {@link User} object, this method will return a new {@link User}.
     *
     * @param nick    The nickname.
     * @param channel The channel this user is in.
     * @return A {@code User} object representing given user.
     */
    public User createUser(String nick, String channel) {
        Channel chan = this.createChannel(channel);
        if (chan.hasUser(nick)) {
            return chan.getUser(nick.toLowerCase());
        } else {
            return this.createUser(nick);
        }
    }

    /**
     * Disconnects from the server. In the case a connection to the server is
     * alive, this method will send the QUIT command and wait for the server to
     * disconnect us.
     */
    public void disconnect() {
        this.disconnect(null);
    }

    /**
     * Disconnects from the server. In the case a connection to the server is
     * alive, this method will send the QUIT command and wait for the server to
     * disconnect us.
     *
     * @param message The QUIT message to use.
     */
    public void disconnect(String message) {
        if (this.isConnected()) {
            this.getOut().sendNow(IrcPacketFactory.createQUIT(message));
        } else {
            this.close();
            this.getState().removeAll();
        }
    }

    /**
     * Gives the advanced listener used by this {@code IrcConnection}.
     *
     * @return The advanced listener, or null.
     */
    public UnknownListener getUnknownListener() {
        return this.unknownListener;
    }

    /**
     * Sets the advanced listener used in this {@code IrcConnection}.
     *
     * @param listener The advanced listener to use, or {@code null}.
     */
    public void setUnknownListener(UnknownListener listener) {
        this.unknownListener = listener;
    }

    /**
     * Returns the character set that is used for the connection's encoding. The
     * default is the system default returned by
     * {@link Charset#defaultCharset()}.
     *
     * @return The character set for the connection's encoding.
     */
    public Charset getCharset() {
        return this.charset;
    }

    /**
     * Sets the character set to use for the connections's encoding. If a
     * connection is already open, it will need to be closed then reopened
     * before any encoding changes will take effect.
     *
     * @param charset The character set to use for the connection's encoding.
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * Returns the client used by this {@code IrcConnection}.
     *
     * @return User representing this client.
     */
    public User getClient() {
        return this.state.getClient();
    }

    /**
     * Returns the outgoing message delay in milliseconds.
     *
     * @return Outgoing message delay in milliseconds.
     */
    public int getMessageDelay() {
        return this.messageDelay;
    }

    /**
     * Sets the outgoing message delay in milliseconds. Note that sending a lot
     * of messages in a short period of time might cause the server to
     * disconnect you. The default is 1 message each 100ms.
     *
     * @param messageDelay The outgoing message delay in milliseconds.
     */
    public void setMessageDelay(int messageDelay) {
        if (messageDelay < 0) {
            throw new IllegalArgumentException(
                    "Message Delay can't be negative!");
        }
        this.messageDelay = messageDelay;
    }

    /**
     * Returns all {@link MessageListener}s registered with this IrcConnection.
     *
     * @return All {@code MessageListeners}.
     */
    public Set<MessageListener> getMessageListeners() {
        return Collections.unmodifiableSet(this.messageListeners);
    }

    /**
     * Returns all {@link ModeListener}s registered with this IrcConnection.
     *
     * @return All {@code ModeListeners}.
     */
    public Set<ModeListener> getModeListeners() {
        return Collections.unmodifiableSet(this.modeListeners);
    }

    /**
     * Returns the output thread used for sending messages through this
     * {@code IrcConnection}.
     *
     * @return The {@code IrcOutput} used to send messages.
     */
    public IrcOutput getOutput() {
        return this.getOut();
    }

    /**
     * Returns the server this {@code IrcConnection} connects to.
     *
     * @return The IRC server.
     */
    public IrcServer getServer() {
        return this.server;
    }

    /**
     * Sets the server details to use while connecting.
     *
     * @param server The server to connect to.
     */
    public void setServer(IrcServer server) {
        if (!this.isConnected()) {
            this.server = server;
        }
    }

    /**
     * Gives the server address this {@code IrcConnection} is using to connect.
     *
     * @return Server address.
     * @since 1.0.0
     */
    public String getServerAddress() {
        return this.server.getAddress();
    }

    /**
     * Sets the server address to use while connecting.
     *
     * @param address The address of the server.
     * @since 1.0.0
     */
    public void setServerAddress(String address) {
        if (!this.isConnected() && (address != null)) {
            this.server.setAddress(address);
        }
    }

    public Set<ServerListener> getServerListeners() {
        return Collections.unmodifiableSet(serverListeners);
    }

    /**
     * Gives the port number this {@code IrcConnection} is using to connect.
     *
     * @return Port number
     * @since 1.0.0
     */
    public int getServerPort() {
        return this.server.getPort();
    }

    /**
     * Sets the server address to use while connecting.
     *
     * @param port The port number to use.
     */
    public void setServerPort(int port) {
        if (!this.isConnected() && (port > 0)) {
            this.server.setPort(port);
        }
    }

    /**
     * Returns all services running on this IrcConnection.
     *
     * @return All running services.
     */
    private Set<SIRCService> getServices() {
        return Collections.unmodifiableSet(this.services);
    }

    /**
     * Retrieves the {@link ClientState} for this {@code IrcConnection}.
     *
     * @return The {@link ClientState}.
     * @since 1.1.0
     */
    public ClientState getState() {
        return this.state;
    }

    /**
     * Gives the version string used.
     *
     * @return The version string.
     * @since 0.9.4
     */
    public String getVersion() {
        if (this.version != null) {
            return this.version;
        }
        return IrcConnection.ABOUT;
    }

    /**
     * Set the string returned on CTCP VERSION and FINGER commands.
     *
     * @param version The string to return on CTCP VERSION and FINGER commands, or
     *                {@code null} to use the default sIRC version string.
     * @since 0.9.4
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns whether this connection is allowed to be redirected.
     *
     * @return {@code true} if redirection is allowed, {@code false} otherwise.
     */
    public boolean isBounceAllowed() {
        return this.bounceAllowed;
    }

    /**
     * Sets whether this connection is allowed to be redirected. If {@code true}
     * , sIRC will change server when it receives a bounce reply.
     *
     * @param bounceAllowed {@code true} if redirection is allowed, {@code false}
     *                      otherwise.
     */
    public void setBounceAllowed(boolean bounceAllowed) {
        this.bounceAllowed = bounceAllowed;
    }

    /**
     * Checks whether the client is still connected.
     *
     * @return True if the client is connected, false otherwise.
     */
    public boolean isConnected() {
        return this.connected;
    }

    /**
     * Changes the connection state of the client.
     *
     * @param connected Whether we are still connected.
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Checks if given {@link User} object represents us.
     *
     * @param user {@code User} to check
     * @return True if given {@code User} represents us, false otherwise.
     */
    public boolean isUs(User user) {
        return user.equals(this.state.getClient());
    }

    /**
     * Checks whether this connection is using SSL.
     *
     * @return True if this connection is using SSL, false otherwise.
     */
    public boolean isUsingSSL() {
        return this.server.isSecure();
    }

    /**
     * Sets whether this connection should use SSL to connect. Note that the
     * connection will fail if the server has no valid certificate. This
     * property can only be changed while sIRC is not connected to an IRC
     * server.
     *
     * @param usingSSL True to use SSL, false otherwise.
     * @see #setServerPort(int)
     */
    public void setUsingSSL(boolean usingSSL) {
        if (!this.isConnected()) {
            this.server.setSecure(usingSSL);
        }
    }

    /**
     * Calls {@link #removeService(SIRCService)} for all registered services.
     *
     * @see #removeService(SIRCService)
     */
    public void removeAllServices() {
        if (this.services.size() > 0) {
            services.forEach(this::removeService);
        }
    }

    /**
     * Removes a message listener from this IrcConnection.
     *
     * @param listener The message listener to remove.
     */
    public void removeMessageListener(MessageListener listener) {
        if ((listener != null) && this.messageListeners.contains(listener)) {
            this.messageListeners.remove(listener);
        }
    }

    /**
     * Removes a mode listener from this IrcConnection.
     *
     * @param listener The mode listener to remove.
     */
    public void removeModeListener(ModeListener listener) {
        if ((listener != null) && this.modeListeners.contains(listener)) {
            this.modeListeners.remove(listener);
        }
    }

    /**
     * Removes a server listener from this IrcConnection.
     *
     * @param listener The server listener to remove.
     */
    public void removeServerListener(ServerListener listener) {
        if ((listener != null) && this.serverListeners.contains(listener)) {
            this.serverListeners.remove(listener);
        }
    }

    /**
     * Remove a service. {@code IrcConnection} will call the
     * {@link SIRCService#unload(IrcConnection)} method of this
     * {@code SIRCService} after removing it the service list.
     *
     * @param service The service to remove.
     */
    public void removeService(SIRCService service) {
        if ((service != null) && this.services.contains(service)) {
            service.unload(this);
            this.services.remove(service);
        }
    }

    /**
     * Marks you as away on the server. If any user sends a message to you while
     * marked as away, the the server will send them a message back.
     *
     * @param reason The reason for being away.
     * @see #setNotAway()
     * @since 1.0.2
     */
    public void setAway(String reason) {
        this.getOut().send(IrcPacketFactory.createAWAY(reason));
    }

    /**
     * Changes the nickname of this client. While connected, this method will
     * attempt to change the nickname on the server.
     *
     * @param nick New nickname.
     */
    public void setNick(String nick) {
        if (!this.isConnected()) {
            if (nick != null) {
                if (this.state.getClient() == null) {
                    this.state.setClient(new User(nick, "sIRC", null, null, this));
                    return;
                }
                this.state.getClient().setNick(nick);
            }
        } else {
            this.getOut().sendNow(IrcPacketFactory.createNICK(nick));
        }
    }

    public void setUsername(String username) {
        setUsername(username, null);
    }

    public void setUsername(String username, String realname) {
        if (!this.isConnected()) {
            if (username != null) {
                if (this.state.getClient() == null) {
                    this.state.setClient(new User(null, username, null, realname, this));
                }
            }
        }
    }

    /**
     * Removes the away mark.
     *
     * @see #setAway(String)
     * @since 1.0.2
     */
    public void setNotAway() {
        this.setAway(null);
    }

    /**
     * Sets the server details to use while connecting.
     *
     * @param address The address of the server.
     * @param port    The port number to use.
     * @since 1.0.0
     */
    public void setServer(String address, int port) {
        this.setServerAddress(address);
        this.setServerPort(port);
    }

    /**
     * Connection OutputStream thread.
     */
    public IrcOutput getOut() {
        return out;
    }
}
