package net.acomputerdog.ircbot.command.util;

public class CommandLine {
    public final String commandLine;
    public final String command;
    public final String args;

    public CommandLine(String commandLine) {
        if (commandLine == null) {
            throw new IllegalArgumentException("Cannot have a null command line!");
        }
        this.commandLine = commandLine;
        if (commandLine.isEmpty()) {
            this.command = "";
            this.args = "";
        } else {
            int split = commandLine.indexOf(' ');
            if (split == -1 || split == commandLine.length() - 1) {
                this.command = commandLine.toLowerCase();
                this.args = "";
            } else {
                this.command = commandLine.substring(0, split).toLowerCase();
                this.args = commandLine.substring(split + 1);
            }
        }
    }

    public boolean hasArgs() {
        return !"".equals(args);
    }

    public boolean hasCommand() {
        return !"".equals(command);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommandLine)) return false;

        CommandLine that = (CommandLine) o;

        return commandLine.equals(that.commandLine);

    }

    @Override
    public int hashCode() {
        return commandLine.hashCode();
    }
}
