package net.acomputerdog.ircbot.command.types;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.Chattable;
import com.sorcix.sirc.User;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import net.acomputerdog.core.java.Sleep;
import net.acomputerdog.core.logger.CLogger;
import net.acomputerdog.ircbot.command.Command;
import net.acomputerdog.ircbot.command.util.CommandLine;
import net.acomputerdog.ircbot.config.Config;
import net.acomputerdog.ircbot.main.IrcBot;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.FilePermission;
import java.security.Permission;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandJavaScript extends Command {

    private final Map<Chattable, JSExecutor> executionMap;

    public CommandJavaScript(IrcBot bot) {
        super(bot, "JavaScript", "javascript", "js");
        executionMap = new ConcurrentHashMap<>();
        //engine = new JSExecutor();
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new JSSecurityManager());
        }
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    executionMap.keySet().forEach(c -> executionMap.get(c).checkRunTime());
                } catch (Throwable t) {
                    getLogger().logError("Exception in JS_Watchdog thread!", t);
                }
                Sleep.sleep(100);
            }
        });
        thread.setName("JS_Watchdog");
        thread.setDaemon(true);
        thread.setPriority(3);
        thread.start();
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "Evaluates a javascript expression.";
    }

    @Override
    public String getHelpString() {
        return Config.COMMAND_PREFIX + "javascript <code>";
    }

    @Override
    public boolean processCommand(IrcBot bot, Channel channel, User sender, Chattable target, CommandLine command) {
        JSExecutor executor = getExecutorFor(target);
        if (executor.code != null) {
            target.send(colorRed("A command is already running, please wait up to 10 seconds for it to finish or be terminated!"));
            return false;
        }
        executor.code = command.args;
        return true;
    }

    private JSExecutor getExecutorFor(Chattable target) {
        JSExecutor executor = executionMap.get(target);
        if (executor == null) {
            executionMap.put(target, executor = new JSExecutor(target));
        }
        return executor;
    }

    private static class JSExecutor {
        private static final String[] jsRemoves = {"Java", "java", "Packages", "packages", "com", "edu", "org", "net", "jdk", "java", "javafx", "javax", "JavaImporter", "sun"};
        private final ScriptEngine engine;
        private final Chattable target;

        private volatile Thread thread;
        private volatile String code = null;
        private volatile long startTime = 0;

        private JSExecutor(Chattable target) {
            this.target = target;
            engine = new NashornScriptEngineFactory().getScriptEngine(new String[]{"--no-java"});
            for (String str : jsRemoves) {
                try {
                    engine.eval(str + " = undefined;");
                } catch (Exception e) {
                    IrcBot.LOGGER.logWarning("Exception removing JS function \"" + str + "\"!", e);
                }
            }
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new JSSecurityManager());
            }
            thread = createThread();
            thread.start();
        }

        private void execute(String code) throws ScriptException {
            target.send(String.valueOf(engine.eval(code)));
        }

        private void checkRunTime() {
            if (startTime != 0 && System.currentTimeMillis() >= startTime + Config.JS_TIMEOUT) {
                startTime = System.currentTimeMillis(); //in case of exception, try again in 10 seconds
                target.send(colorRed("[Script Error] JS engine timed out!  Your script either hang or took to long to execute, and was terminated!"));
                // Bad, dont use -> thread.stop(new IllegalStateException("JS execution timeout"));
                thread.stop();
                code = null;
                startTime = 0;
                thread = createThread();
                thread.start();
            }
        }

        private Thread createThread() {
            Runnable runnable = () -> {
                while (true) {
                    if (code != null) {
                        startTime = System.currentTimeMillis();
                        String codeCopy = code; //do NOT execute this!  Its only for printing out the error-ed code
                        try {
                            JSSecurityManager.setActive(true);
                            execute(code);
                            JSSecurityManager.setActive(false);
                        } catch (ThreadDeath e) {
                            //message is sent elsewhere
                            IrcBot.LOGGER.logWarning("JS timed out \"" + codeCopy + "\".");
                        } catch (ScriptException e) {
                            target.send(colorRed("[Script Error] " + e.getMessage()));
                        } catch (SecurityException e) {
                            target.send(colorRed("[Script Error] Script attempted to perform an illegal operation!"));
                        } catch (Throwable t) {
                            target.send(colorRed("An exception occurred simulating your script!"));
                            target.send(colorRed("Exception type is: \"" + t.getClass().getName() + "\".  Message: \"" + t.getMessage() + "\"."));
                            IrcBot.LOGGER.logError("Exception executing JS \"" + code + "\"!", t);
                        }
                        startTime = 0;
                        code = null;
                    }
                    Sleep.sleep(100);
                }
            };
            Thread thread = new Thread(runnable);
            thread.setName("JS_Executor");
            thread.setPriority(3);
            thread.setDaemon(true);
            return thread;
        }
    }

    private static class JSSecurityManager extends SecurityManager {
        private static final String[] allowedPerms = {"nashorn.setConfig", "stopThread", "accessClassInPackage", "suppressAccessChecks", "accessDeclaredMembers", "createClassLoader", "getClassLoader"};

        private static boolean active = false;

        private final SecurityManager parent;
        private final CLogger LOGGER;

        private JSSecurityManager() {
            super();
            parent = System.getSecurityManager();
            LOGGER = new CLogger("JSSecurity", false, true);
        }

        @Override
        public void checkPermission(Permission perm) {
            if (active) {
                Class[] stack = getClassContext();
                String permName = perm.getName();
                boolean isAllowed = (perm instanceof FilePermission) && permName.endsWith(".jar"); //loading JS engine internals
                if (!isAllowed) {
                    for (String str : allowedPerms) {
                        if (permName.startsWith(str)) {
                            isAllowed = true;
                            break;
                        }
                    }
                }

                if (!isAllowed) {
                    for (Class cls : stack) {
                        if (JSExecutor.class.equals(cls)) {
                            if (JSExecutor.class.equals(cls)) {
                                LOGGER.logWarning("Attempted to execute code from JS!  Permission:" + perm.toString());
                                throw new SecurityException("JavaScript function blocked!");
                            }
                        }
                    }
                }
            }
            if (parent != null) {
                parent.checkPermission(perm);
            }
        }

        private static void setActive(boolean active) {
            JSSecurityManager.active = active;
        }
    }

}
