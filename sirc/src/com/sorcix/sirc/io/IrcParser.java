/*
 * IrcParser.java
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
package com.sorcix.sirc.io;

import com.sorcix.sirc.listener.ModeListener;
import com.sorcix.sirc.main.IrcConnection;
import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.IrcServer;
import com.sorcix.sirc.structure.User;

import java.util.Date;

/**
 * Parses incoming messages and calls event handlers.
 *
 * @author Sorcix
 */
public class IrcParser {

    /**
     * Buffer for motd.
     */
    private StringBuffer motdBuffer = null;

    /**
     * Parses normal IRC commands.
     *
     * @param irc  IrcConnection receiving this line.
     * @param line The input line.
     */
    public void parseCommand(IrcConnection irc, IrcPacket line) {
        if (line.getCommand().equals("PRIVMSG") && (line.getArguments() != null)) {
            if (line.isCtcp()) {
                // reply to CTCP commands
                if (line.getMessage().startsWith("ACTION ")) {
                    if (Channel.CHANNEL_PREFIX.indexOf(line.getArguments().charAt(0)) >= 0) {
                        // to channel
                        Channel chan = irc.getState().getChannel(line.getArguments());
                        irc.getMessageListeners().forEach(l -> l.onAction(irc, chan.updateUser(line.getSender(), true), chan, line.getMessage().substring(7)));
                    } else {
                        // to user
                        irc.getMessageListeners().forEach(l -> l.onAction(irc, line.getSender(), line.getMessage().substring(7)));
                    }
                } else if (line.getMessage().equals("VERSION") || line.getMessage().equals("FINGER")) {
                    // send custom version string
                    line.getSender().sendCtcpReply("VERSION " + irc.getVersion());
                } else if (line.getMessage().equals("SIRCVERS")) {
                    // send sIRC version information
                    line.getSender().sendCtcpReply("SIRCVERS " + IrcConnection.ABOUT + " + " + IrcConnection.ABOUT_ADDITIONAL);
                } else if (line.getMessage().equals("TIME")) {
                    // send current date&time
                    line.getSender().sendCtcpReply(new Date().toString());
                } else if (line.getMessage().startsWith("PING ")) {
                    // send ping reply
                    line.getSender().sendCtcpReply("PING " + line.getMessage().substring(5), true);
                } else if (line.getMessage().startsWith("SOURCE")) {
                    // send acombuterbot sources.  Previously sent sIRC, but acomputerbot has a link to sIRC sources so its OK to remove
                    // line.getSender().sendCtcpReply("SOURCE http://j-sirc.googlecode.com");
                    line.getSender().sendCtcpReply("SOURCE https://github.com/warriordog/acomputerbot");
                } else if (line.getMessage().equals("CLIENTINFO")) {
                    // send client info
                    line.getSender().sendCtcpReply("CLIENTINFO VERSION TIME PING SOURCE FINGER SIRCVERS");
                } else if (line.getMessage().equals("ACOMPUTERBOTVERS")) {
                    line.getSender().sendCtcpReply("ACOMPUTERBOTVERS Acomputerbot - (" + IrcConnection.ABOUT_ADDITIONAL + ")");
                } else {
                    // send error message
                    line.getSender().sendCtcpReply("ERRMSG CTCP Command not supported. Use CLIENTINFO to list supported commands.");
                }
            } else if (line.getArguments().startsWith("#") || line.getArguments().startsWith("&")) {
                // to channel
                Channel chan = irc.getState().getChannel(line.getArguments());
                irc.getMessageListeners().forEach(l -> l.onMessage(irc, chan.updateUser(line.getSender(), true), chan, line.getMessage()));
            } else {
                // to user
                irc.getMessageListeners().forEach(l -> l.onPrivateMessage(irc, line.getSender(), line.getMessage()));
            }
        } else if (line.getCommand().equals("NOTICE") && (line.getArguments() != null)) {
            if (line.isCtcp()) {
                // receive CTCP replies.
                int cmdPos = line.getMessage().indexOf(' ');
                String command = line.getMessage().substring(0, cmdPos);
                String args = line.getMessage().substring(cmdPos + 1);
                if (command.equals("VERSION") || command.equals("PING") || command.equals("CLIENTINFO")) {
                    irc.getMessageListeners().forEach(l -> l.onCtcpReply(irc, line.getSender(), command, args));
                }
            } else if (Channel.CHANNEL_PREFIX.indexOf(line.getArguments().charAt(0)) >= 0) {
                // to channel
                Channel chan = irc.getState().getChannel(line.getArguments());
                irc.getMessageListeners().forEach(l -> l.onNotice(irc, chan.updateUser(line.getSender(), true), chan, line.getMessage()));
            } else {
                // to user
                irc.getMessageListeners().forEach(l -> l.onNotice(irc, line.getSender(), line.getMessage()));
            }
        } else if (line.getCommand().equals("JOIN")) {
            // some server seem to send the joined channel as message,
            // while others have it as an argument. (quakenet related)
            String channel;
            if (line.hasMessage()) {
                channel = line.getMessage();
            } else {
                channel = line.getArguments();
            }
            // someone joined a channel
            if (line.getSender().isUs()) {
                // if the user joining the channel is the client
                // we need to add it to the channel list.
                irc.getState().addChannel(new Channel(channel, irc, true));
            } else {
                // add user to channel list.
                irc.getState().getChannel(channel).addUser(line.getSender());
            }
            irc.getServerListeners().forEach(l -> l.onJoin(irc, irc.getState().getChannel(channel), line.getSender()));
        } else if (line.getCommand().equals("PART")) {
            // someone left a channel
            if (line.getSender().isUs()) {
                // if the user leaving the channel is the client
                // we need to remove it from the channel list
                irc.getState().removeChannel(line.getArguments());
            } else {
                // remove user from channel list.
                irc.getState().getChannel(line.getArguments()).removeUser(line.getSender());
            }
            irc.getServerListeners().forEach(l -> l.onPart(irc, irc.getState().getChannel(line.getArguments()), line.getSender(), line.getMessage()));
        } else if (line.getCommand().equals("QUIT")) {
            // someone quit the IRC server
            User quitter = line.getSender();
            irc.getServerListeners().forEach(l -> l.onQuit(irc, quitter, line.getMessage()));
            irc.getState().getChannelMap().values().stream().filter(channel -> channel.hasUser(quitter)).forEach(channel -> channel.removeUser(quitter));
        } else if (line.getCommand().equals("KICK")) {
            // someone was kicked from a channel
            String[] data = line.getArgumentsArray();
            User kicked = new User(data[1], irc);
            Channel channel = irc.getState().getChannel(data[0]);
            if (kicked.isUs()) {
                // if the user leaving the channel is the client
                // we need to remove it from the channel list
                irc.getState().removeChannel(data[0]);
            } else {
                // remove user from channel list.
                channel.removeUser(kicked);
            }
            irc.getServerListeners().forEach(l -> l.onKick(irc, channel, line.getSender(), kicked, line.getMessage()));
        } else if (line.getCommand().equals("MODE")) {
            this.parseMode(irc, line);
        } else if (line.getCommand().equals("TOPIC")) {
            // someone changed the topic.
            Channel chan = irc.getState().getChannel(line.getArguments());
            irc.getServerListeners().forEach(l -> l.onTopic(irc, chan, chan.updateUser(line.getSender(), false), line.getMessage()));
        } else if (line.getCommand().equals("NICK")) {
            User newUser;
            if (line.hasMessage()) {
                newUser = new User(line.getMessage(), irc);
            } else {
                newUser = new User(line.getArguments(), irc);
            }
            // someone changed his nick
            irc.getState().getChannelMap().values().forEach(channel -> channel.renameUser(line.getSender().getNickLower(), newUser.getNick()));
            // change local user
            if (line.getSender().isUs()) {
                irc.getState().getClient().setNick(newUser.getNick());
            }
            irc.getServerListeners().forEach(l -> l.onNick(irc, line.getSender(), newUser));
        } else if (line.getCommand().equals("INVITE")) {
            // someone was invited
            String[] args = line.getArgumentsArray();
            if ((args.length >= 2) && (line.getMessage() == null)) {
                Channel channel = irc.createChannel(args[1]);
                irc.getServerListeners().forEach(l -> l.onInvite(irc, line.getSender(), new User(args[0], irc), channel));
            }
        } else {
            if (irc.getUnknownListener() != null) {
                irc.getUnknownListener().onUnknown(irc, line);
            }
        }
    }

    /**
     * Parses mode changes.
     *
     * @param irc  IrcConnection receiving this line.
     * @param line The mode change line.
     */
    private void parseMode(IrcConnection irc, IrcPacket line) {
        String[] args = line.getArgumentsArray();
        if ((args.length >= 2) && (Channel.CHANNEL_PREFIX.indexOf(args[0].charAt(0)) >= 0)) {
            // general mode event listener
            irc.getServerListeners().forEach(l -> l.onMode(irc, irc.getState().getChannel(args[0]), line.getSender(), line.getArguments().substring(args[0].length() + 1)));
            if ((args.length >= 3)) {
                Channel channel = irc.getState().getChannel(args[0]);
                String mode = args[1];
                boolean enable = mode.charAt(0) == '+';
                char current;
                // tries all known modes.
                // this is an ugly part of sIRC, but the only way to
                // do this.
                for (int x = 2; x < args.length; x++) {
                    current = mode.charAt(x - 1);
                    if (current == User.MODE_VOICE) {
                        // voice or devoice
                        irc.askNames(channel);
                        if (enable) {
                            for (ModeListener listener : irc.getModeListeners()) {
                                listener.onVoice(irc, channel, line.getSender(), irc.createUser(args[x]));
                            }
                        } else {
                            for (ModeListener listener : irc.getModeListeners()) {
                                listener.onDeVoice(irc, channel, line.getSender(), irc.createUser(args[x]));
                            }
                        }
                    } else if (current == User.MODE_ADMIN) {
                        // admin or deadmin
                        irc.askNames(channel);
                        if (enable) {
                            for (ModeListener listener : irc.getModeListeners()) {
                                listener.onAdmin(irc, channel, line.getSender(), irc.createUser(args[x]));
                            }
                        } else {
                            for (ModeListener listener : irc.getModeListeners()) {
                                listener.onDeAdmin(irc, channel, line.getSender(), irc.createUser(args[x]));
                            }
                        }
                    } else if (current == User.MODE_OPERATOR) {
                        // op or deop
                        irc.askNames(channel);
                        if (enable) {
                            for (ModeListener listener : irc.getModeListeners()) {
                                listener.onOp(irc, channel, line.getSender(), irc.createUser(args[x]));
                            }
                        } else {
                            for (ModeListener listener : irc.getModeListeners()) {
                                listener.onDeOp(irc, channel, line.getSender(), irc.createUser(args[x]));
                            }
                        }
                    } else if (current == User.MODE_HALF_OP) {
                        // halfop or dehalfop
                        irc.askNames(channel);
                        if (enable) {
                            for (ModeListener listener : irc.getModeListeners()) {
                                listener.onHalfop(irc, channel, line.getSender(), irc.createUser(args[x]));
                            }
                        } else {
                            for (ModeListener listener : irc.getModeListeners()) {
                                listener.onDeHalfop(irc, channel, line.getSender(), irc.createUser(args[x]));
                            }
                        }
                    } else if (current == User.MODE_FOUNDER) {
                        // founder or defounder
                        irc.askNames(channel);
                        if (enable) {
                            for (ModeListener listener : irc.getModeListeners()) {
                                listener.onFounder(irc, channel, line.getSender(), irc.createUser(args[x]));
                            }
                        } else {
                            for (ModeListener listener : irc.getModeListeners()) {
                                listener.onDeFounder(irc, channel, line.getSender(), irc.createUser(args[x]));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Parses numeric IRC replies.
     *
     * @param irc  IrcConnection receiving this line.
     * @param line The input line.
     */
    protected void parseNumeric(IrcConnection irc, IrcPacket line) {
        switch (line.getNumericCommand()) {
            case IrcPacket.RPL_TOPIC:
                irc.getServerListeners().forEach((l -> l.onTopic(irc, irc.getState().getChannel(line.getArgumentsArray()[1]), null, line.getMessage())));
                break;
            case IrcPacket.RPL_NAMREPLY:
                String[] arguments = line.getArgumentsArray();
                Channel channel = irc.getState().getChannel(arguments[arguments.length - 1]);
                if (channel != null) {
                    String[] users = line.getMessage().split(" ");
                    User buffer;
                    for (String user : users) {
                        buffer = new User(user, irc);
                        /*
                         * if (channel.hasUser(motdBuffer)) {
						 * channel.addUser(motdBuffer); }
						 * channel.addUser(motdBuffer);
						 */
                        channel.updateUser(buffer, true);
                    }
                }
                break;
            case IrcPacket.RPL_MOTD:
                if (this.motdBuffer == null) {
                    this.motdBuffer = new StringBuffer();
                }
                this.motdBuffer.append(line.getMessage());
                this.motdBuffer.append(IrcConnection.ENDLINE);
                break;
            case IrcPacket.RPL_ENDOFMOTD:
                if (this.motdBuffer != null) {
                    String motd = this.motdBuffer.toString();
                    this.motdBuffer = null;
                    irc.getServerListeners().forEach(l -> l.onMotd(irc, motd));
                }
                break;
            case IrcPacket.RPL_BOUNCE:
                // redirect to another server.
                if (irc.isBounceAllowed()) {
                    irc.disconnect();
                    irc.setServer(new IrcServer(line.getArgumentsArray()[0], line.getArgumentsArray()[1]));
                    try {
                        irc.connect();
                    } catch (Exception ex) {
                        // TODO: exception while connecting to new
                        // server?
                    }
                }
                break;
            default:
                if (irc.getUnknownListener() != null) {
                    irc.getUnknownListener().onUnknown(irc, line);
                }
        }
    }
}
