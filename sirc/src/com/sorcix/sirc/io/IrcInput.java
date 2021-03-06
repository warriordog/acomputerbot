/*
 * IrcInput.java
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

import com.sorcix.sirc.main.IrcConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Input Thread.
 *
 * @author Sorcix
 */
public class IrcInput extends Thread {

    /**
     * Stream used to read from the IRC server.
     */
    private final BufferedReader in;
    /**
     * The IrcConnection.
     */
    private final IrcConnection irc;

    private final IrcParser parser = new IrcParser();

    /**
     * Creates a new input thread.
     *
     * @param irc The IrcConnection using this output thread.
     * @param in  The stream to use for communication.
     */
    public IrcInput(IrcConnection irc, Reader in) {
        this.setName("sIRC-IN:" + irc.getServerAddress() + "-" + irc.getClient().getUserName());
        this.setPriority(Thread.NORM_PRIORITY);
        this.setDaemon(false);
        this.in = new BufferedReader(in);
        this.irc = irc;
    }

    /**
     * Closes the input stream.
     *
     * @throws IOException
     * @see IrcConnection#disconnect()
     */
    public void close() throws IOException {
        this.in.close();
    }

    /**
     * Returns the reader used in this input thread.
     *
     * @return the reader.
     */
    public BufferedReader getReader() {
        return this.in;
    }

    /**
     * Handles a line received by the IRC server.
     *
     * @param line The line to handle.
     */
    private void handleLine(String line) {
        // transform the raw line into an easier format
        IrcPacket parser = new IrcPacket(line, this.irc);
        // Handle numeric server replies.
        if (parser.isNumeric()) {
            this.parser.parseNumeric(this.irc, parser);
            return;
        }
        // Handle different commands
        this.parser.parseCommand(this.irc, parser);
    }

    /**
     * Checks the input stream for new messages.
     */
    @Override
    public void run() {
        String line;
        try {
            // wait for lines to come in
            while ((line = this.in.readLine()) != null) {
                //IrcDebug.log("<<< " + line);
                // always respond to PING
                if (line.startsWith("PING ")) {
                    this.irc.getOut().pong(line.substring(5));
                } else {
                    this.handleLine(line);
                }
            }
        } catch (IOException ex) {
            this.irc.setConnected(false);
        } catch (Exception ex) {
            //IrcDebug.log("Exception " + ex + " on: " + line);
            ex.printStackTrace();
        }
        // when reaching this, we are disconnected
        this.irc.setConnected(false);
        // close connections
        this.irc.disconnect();
        // send disconnect event
        irc.getServerListeners().forEach(listener -> listener.onDisconnect(irc));
    }
}
