package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.structure.Channel;
import com.sorcix.sirc.structure.User;
import com.sorcix.sirc.util.Chattable;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class CommandJavaScriptConsole extends Command {

    private final ScriptEngine engine;

    public CommandJavaScriptConsole(IrcBot bot) {
        super(bot, "JavaScriptConsole", "javascriptconsole", "javascript-console", "javascript_console", "jsc");
        engine = new NashornScriptEngineFactory().getScriptEngine();
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public boolean requiresAdmin() {
        return true;
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "javascriptconsole <code>";
    }

    @Override
    public String getDescription() {
        return "Runs a javascript console with access to control AcomputerBot";
    }

    @Override
    public boolean processCommand(Channel channel, User sender, Chattable target, CommandLine command) {
        if (!Config.ENABLE_CONSOLE) {
            target.send(colorRed("JavaScript console is disabled!"));
            return false;
        }
        try {
            bot.getStringCheck().sendFormattedString(String.valueOf(engine.eval(command.args)), target, "> ", true);
            getLogger().logInfo(target.getName() + " executed CONSOLE JS: \"" + command.args + "\".");
            return true;
        } catch (ScriptException e) {
            target.send(colorRed("[Script Error] " + e.getMessage()));
            getLogger().logInfo(target.getName() + " wrote invalid CONSOLE JS: \"" + command.args + "\".");
            return false;
        } catch (Throwable t) {
            target.send(colorRed("An exception occurred simulating your script!"));
            target.send(colorRed("Exception type is: \"" + t.getClass().getName() + "\".  Message: \"" + t.getMessage() + "\"."));
            getLogger().logError(target.getName() + " wrote bugged CONSOLE JS: \"" + command.args + "\"!", t);
            return false;
        }
    }
}
