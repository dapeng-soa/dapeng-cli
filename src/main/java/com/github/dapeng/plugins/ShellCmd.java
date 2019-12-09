package com.github.dapeng.plugins;

import com.github.dapeng.utils.CmdProperties;
import com.github.dapeng.utils.CmdUtils;
import com.github.dapeng.utils.ShellUtils;
import org.clamshellcli.api.Command;
import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author hui
 * 2019/12/9 0009 11:01
 */
public class ShellCmd implements Command {
    private static final Logger logger = LoggerFactory.getLogger(ShellCmd.class);
    private static final String NAMESPACE = "syscmd";
    private static final String ACTION_NAME = "shell";

    @Override
    public Descriptor getDescriptor() {
        return new Command.Descriptor() {

            @Override
            public String getNamespace() {
                return NAMESPACE;
            }

            @Override
            public String getName() {
                return ACTION_NAME;
            }

            @Override
            public String getDescription() {
                return "execute the shell script";
            }

            @Override
            public String getUsage() {
                StringBuilder result = new StringBuilder();
                result.append(Configurator.VALUE_LINE_SEP).append(" shell script command");
                result.append(Configurator.VALUE_LINE_SEP).append(" shell -f fileName");
                return result.toString();
            }

            Map<String, String> args = null;

            @Override
            public Map<String, String> getArguments() {
                if (null != args) {
                    return args;
                }
                args = new LinkedHashMap<>();
                args.put(CmdProperties.KEY_ARGS_FILE_READ, "type '-f' read shell script file ");
                return args;
            }
        };
    }

    @Override
    public Object execute(Context context) {
        Map<String, String> inputArgs = CmdUtils._getCmdArgs(context);
        String scriptFile = inputArgs.get(CmdProperties.KEY_ARGS_FILE_READ);
        String command = inputArgs.get(ACTION_NAME);
        logger.info("[execute] ==>inputArgs=[{}]", inputArgs);
        if (!CmdUtils.isEmpty(scriptFile)) {
            ShellUtils.executeShellScriptFromFile(scriptFile);
        } else if (null != command) {
            ShellUtils.executeShellScript(command);
        }
        return null;
    }

    @Override
    public void plug(Context context) {

    }

    @Override
    public void unplug(Context context) {

    }
}
