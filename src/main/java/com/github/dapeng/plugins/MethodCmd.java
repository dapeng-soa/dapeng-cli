package com.github.dapeng.plugins;

import com.github.dapeng.utils.CmdProperties;
import com.github.dapeng.utils.CmdUtils;
import org.clamshellcli.api.Command;
import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 获得service 方法列表
 *
 * @author huyj
 * @Created 2018/5/10 17:50
 */
public class MethodCmd implements Command {
    private static final Logger logger = LoggerFactory.getLogger(MethodCmd.class);

    private static final String NAMESPACE = "syscmd";
    private static final String ACTION_NAME = "method";

    @Override
    public Command.Descriptor getDescriptor() {
        return new Command.Descriptor() {
            public String getNamespace() {
                return NAMESPACE;
            }

            public String getName() {
                return ACTION_NAME;
            }

            public String getDescription() {
                return "list the Service Method. ";
            }

            public String getUsage() {
                StringBuilder sb = new StringBuilder();
                sb.append(Configurator.VALUE_LINE_SEP).append(" method -s");
                return sb.toString();
            }

            Map<String, String> args = null;

            public Map<String, String> getArguments() {
                if (args != null) return args;
                args = new LinkedHashMap();
                args.put(CmdProperties.KEY_ARGS_SERVICE, "type '-s [serviceName]' to get the service method list.....");
                return args;
            }
        };
    }

    /**
     * 1. 获取运行时服务的 方法列表    mothod  -s
     *
     * @param context
     * @return
     */
    @Override
    public Object execute(Context context) {
        Map<String, String> inputArgs = CmdUtils.getCmdArgs(context);
        logger.info("[execute] ==> inputArgs=[{}]", inputArgs);
        String serviceName = inputArgs.get(CmdProperties.KEY_ARGS_SERVICE);

        //1. 获取服务列表
        if (serviceName != null) {
            List<String> mothods = CmdUtils.getRuntimeServiceMethods(context, serviceName);
            if (mothods != null && !mothods.isEmpty()) {
                mothods.forEach(i -> {
                    CmdUtils.writeMsg(context, i);
                });
            } else {
                CmdUtils.writeMsg(context, " the service[" + serviceName + "] not found method ... ");
            }
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