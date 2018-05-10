package com.github.dapeng.plugins.commands;

import com.github.dapeng.utils.ZookeeperUtils;
import jline.console.ConsoleReader;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.NullCompleter;
import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.InputController;
import org.clamshellcli.impl.CliConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

public class ZkController implements InputController {

    private static final Logger logger = LoggerFactory.getLogger(ZkController.class);
    private static final String DAPENG_NAMESPACE = "dapeng";
    private static String COMMANDS_DIR_NAME = "commands";
    private static String CLASSES_DIR_NAME = "classes";
    private static Class COMMAND_TYPE = Command.class;
    private Map<String, Command> commands;
    private String respondsToRegEx = "(zk)\\b(.*)";
    //private String respondsToRegEx = "\\b(.*)";
    private Pattern respondsTo;
    private boolean enable = true;

    public ZkController() {
        this.respondsTo = Pattern.compile(this.respondsToRegEx);
    }

    public Pattern respondsTo() {
        return this.respondsTo;
    }

    @Override
    public void setInputPattern(Pattern pattern) {

    }

    @Override
    public Boolean isEnabled() {
        return enable;
    }

    @Override
    public void setEnabled(Boolean aBoolean) {
        enable = aBoolean;
    }

    @Override
    public void plug(Context plug) {
        List<Command> allCmds = this.loadCommands(plug);
        if (allCmds.size() > 0) {
            plug.putValue("key.commands", allCmds);
            this.commands = plug.mapCommands(allCmds);
            ZkCompleter completer = new ZkCompleter(allCmds);
            ConsoleReader console = ((CliConsole) plug.getIoConsole()).getReader();
            console.addCompleter(completer);
            console.addCompleter(new FileNameCompleter());
            console.addCompleter(NullCompleter.INSTANCE);

            //初始化zookeeper
            ZookeeperUtils.connect();
        } else {
            //plug.getIoConsole().printf("%nNo commands were found for input controller [%s].%n", new Object[]{this.getClass().getName()});
        }

    }

    @Override
    public void unplug(Context context) {

    }


    @Override
    public boolean handle(Context ctx) {
        boolean handled = false;
        /*String cmdLine = (String) ctx.getValue("key.commandlineInput");
           logger.info("[handle] ==>step into handle... cmdLine=[{}],this.respondsTo=[{}]",cmdLine, this.respondsTo);
        logger.info("[handle] ==>respondsTo.matcher(cmdLine).matches()=[{}]", respondsTo.matcher(cmdLine).matches());
        if (cmdLine != null && !cmdLine.trim().isEmpty() && this.respondsTo.matcher(cmdLine).matches()) {
            String[] tokens = cmdLine.trim().split("\\s+");
            logger.info("[handle] ==> commands=[{}],tokens = [{}]",commands,tokens);
            if (this.commands != null && !this.commands.isEmpty()) {
                Command cmd = (Command) this.commands.get(tokens[0]);
                if (cmd != null) {
                    if (tokens.length > 1) {
                        String[] args = (String[]) Arrays.copyOfRange(tokens, 1, tokens.length);
                        ctx.putValue("key.commandParams", args);
                    }

                    try {
                        cmd.execute(ctx);
                    } catch (Exception var7) {
                        ctx.getIoConsole().printf("WARNING: unable to execute command: [%s]%n%s%n", new Object[]{cmdLine, var7.getMessage()});
                    }
                } else {
                    ctx.getIoConsole().printf("%nCommand [%s] is unknown. Type help for a list of installed commands.", new Object[]{tokens[0]});
                }

                handled = true;
            }
        }*/

        return handled;
    }


    private List<Command> loadCommands(Context plug) {
        logger.info("[loadCommands] ==> Step into load commands....");
        ServiceLoader loader = ServiceLoader.load(Command.class, this.getClass().getClassLoader());
        logger.info("loadCommands ==> ServiceLoader: {}", loader);
        List<Command> result = new ArrayList<>();
        Iterator<Command> commands = loader.iterator();
        while (commands.hasNext()) {
            Command command = commands.next();
            result.add(command);
            logger.info(" Dapeng load command:[{}]{}", command.getDescriptor().getName(),command);
        }
        return result;
    }

}
