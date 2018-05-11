package com.github.dapeng.plugins;

import com.github.dapeng.openapi.cache.ServiceCache;
import com.github.dapeng.utils.CmdProperties;
import com.github.dapeng.utils.CmdUtils;
import com.github.dapeng.utils.ZookeeperUtils;
import org.clamshellcli.api.Command;
import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceCmd implements Command {
    private static final Logger logger = LoggerFactory.getLogger(ServiceCmd.class);
    private static final String NAMESPACE = "syscmd";
    private static final String ACTION_NAME = "service";

    @Override
    public Descriptor getDescriptor() {
        return new Command.Descriptor() {
            public String getNamespace() {
                return NAMESPACE;
            }

            public String getName() {
                return ACTION_NAME;
            }

            public String getDescription() {
                return "list the zk runtime Service . ";
            }

            public String getUsage() {
                StringBuilder sb = new StringBuilder();
                sb.append(Configurator.VALUE_LINE_SEP).append(" service   -l");

                return sb.toString();
            }

            Map<String, String> args = null;

            public Map<String, String> getArguments() {
                if (args != null) return args;
                args = new LinkedHashMap();
                args.put(CmdProperties.KEY_ARGS_SERVICE_LIST, "type '-l ' to get runtime service list.....");
                return args;
            }
        };
    }

    /**
     * 1. 获取运行时服务列表    service -l
     *
     * @param context
     * @return
     */
    @Override
    public Object execute(Context context) {
        Map<String, String> inputArgs = CmdUtils.getCmdArgs(context);
        String list = inputArgs.get(CmdProperties.KEY_ARGS_SERVICE_LIST);

        logger.info("[execute] ==>inputArgs=[{}]", inputArgs);
        //1. 获取服务列表
        if (list != null) {
            List<String> services = getRuntimeService();
            logger.info("[execute] ==>services=[{}]", services);
            //List<String> services = ZookeeperUtils.getRuntimeServices();
            if (services != null && !services.isEmpty()) {
                services.forEach(i -> {
                    CmdUtils.writeMsg(context, i);
                });
            } else {
                CmdUtils.writeMsg(context, " no runtime service found........ ");
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


    private List<String> getRuntimeService() {
        logger.info("[getRuntimeService] ==>!ZookeeperUtils.isContextInitialized()=[{}]",!ZookeeperUtils.isContextInitialized());
        if (!ZookeeperUtils.isContextInitialized()) {
            ZookeeperUtils.connect();
        }

        List<String> services = ServiceCache.getServices().entrySet().stream().map(i -> i.getValue().getNamespace() + "." + i.getKey()).collect(Collectors.toList());
        Collections.sort(services);
        return services;
    }

}
