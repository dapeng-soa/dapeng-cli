package com.github.dapeng.plugins;

import com.github.dapeng.router.Route;
import com.github.dapeng.router.RoutesExecutor;
import com.github.dapeng.utils.CmdProperties;
import com.github.dapeng.utils.CmdUtils;
import com.github.dapeng.utils.ServiceUtils;
import com.github.dapeng.utils.ZookeeperUtils;
import org.clamshellcli.api.Command;
import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ZkCmd implements Command {
    private static final Logger logger = LoggerFactory.getLogger(ZkCmd.class);
    private static final String NAMESPACE = "dapeng";
    private static final String ACTION_NAME = "zk";

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
                return " Zk related operations, default zkHost: 127.0.0.1:2181, your can use 'set' cmd to specific your zkHost.";
            }

            public String getUsage() {
                StringBuilder sb = new StringBuilder();
                sb.append(Configurator.VALUE_LINE_SEP)
                        .append(" zk -get path ")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(" zk -nodes path ")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(" zk -set path -d data ")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(" zk -route path -d data ")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append("zk [options]").append(Configurator.VALUE_LINE_SEP);
//
//                for(Map.Entry<String,String> entry : getArguments().entrySet()){
//
//                    sb.append(String.format("%n%1$5s", entry.getKey()) + "        " + entry.getValue());
//                }
//
//                sb.append(Configurator.VALUE_LINE_SEP);
                return sb.toString();
            }

            Map<String, String> args = null;

            public Map<String, String> getArguments() {
                if (args != null) return args;
                args = new LinkedHashMap<String, String>();
                args.put(CmdProperties.KEY_ARGS_ZK_GET, "[optional] type '-get path' to get zk node data by path.");
                args.put(CmdProperties.KEY_ARGS_ZK_SET, "[optional] type '-set path -d data' to set zk node data by path. Note: '/soa/runtime/services' subPath can not be setting! ");
                args.put(CmdProperties.KEY_ARGS_ZK_NODE, "[optional] type '-nodes path' to specific method");
                args.put(CmdProperties.KEY_ARGS_DATA, "[optional] 'you should specific '-set path' option before you set data, like: '-set path -d data'");
                args.put(CmdProperties.KEY_ARGS_ZK_ROUTE, "[optional] 'you should specific '-route path' option before you set data, like: '-route path -d data'. Note:set data before will check must conform to the routing rules ");
                args.put(CmdProperties.KEY_ARGS_FILE, "type '-f file(path + fileName)' to get zkData content for invoking..");

                return args;
            }
        };
    }

    @Override
    public Object execute(Context context) {
        logger.info("[execute] ==> zk command execute....");
        //Map<String, String> inputArgs = CmdUtils.getCmdArgs(context);
        Map<String, String> inputArgs = CmdUtils._getCmdArgs(context);
        logger.info("[execute] ==>inputArgs =[{}]", inputArgs);
        String getArgs = inputArgs.get(CmdProperties.KEY_ARGS_ZK_GET);
        String setArgs = inputArgs.get(CmdProperties.KEY_ARGS_ZK_SET);
        String dataArgs = inputArgs.get(CmdProperties.KEY_ARGS_DATA);
        String nodesArgs = inputArgs.get(CmdProperties.KEY_ARGS_ZK_NODE);
        String routeArgs = inputArgs.get(CmdProperties.KEY_ARGS_ZK_ROUTE);
        String fileName = inputArgs.get(CmdProperties.KEY_ARGS_FILE);

        boolean handled = false;
        //处理  zk -get path
        if (!CmdUtils.isEmpty(getArgs)) {
            logger.info("[execute] ==> handle  zk -get path ...");
            String data = ZookeeperUtils.getData(getArgs);
            CmdUtils.writeMsg(context, "zookeeperData: " + data);
            handled = true;
        }

        //处理  zk -set path -d data
        if (!CmdUtils.isEmpty(setArgs) && !CmdUtils.isEmpty(dataArgs)) {
            logger.info("[execute] ==> handle  zk -set path -d data ...");
            if (setArgs.startsWith(CmdProperties.RUNTIME_PATH)) {
                CmdUtils.writeMsg(context, CmdProperties.RUNTIME_PATH + " is protected.. it can not be setting.");
                return null;
            } else {
                ZookeeperUtils.createData(setArgs, dataArgs);
                CmdUtils.writeMsg(context, " Zookeeper path: " + setArgs + " setting data :[" + dataArgs + "] Successfully.");
                handled = true;
            }
        }

        //处理  zk -route path -d data (or -f  filePath)
        if (!CmdUtils.isEmpty(routeArgs)) {
            if (routeArgs.startsWith(CmdProperties.RUNTIME_PATH)) {
                CmdUtils.writeMsg(context, CmdProperties.RUNTIME_PATH + " is protected.. it can not be setting.");
                return null;
            }

            String routeData = null;
            if (!CmdUtils.isEmpty(dataArgs)) {
                routeData = dataArgs;
            } else if (!CmdUtils.isEmpty(fileName)) {
                routeData = ServiceUtils.readFromeFile(fileName);
            }

            //检查路由语法格式
            logger.info("[execute] ==>routeData=[{}]", routeData);

            List<Route> routes = null;
            try {
                routes = RoutesExecutor.parseAll(routeData);
            } catch (Exception e) {
                logger.info("parse route failed ...route configuration format is incorrect. exception:{}",e.getMessage());
                //e.printStackTrace();
            }
            if (routes != null && !routes.isEmpty()) {
                ZookeeperUtils.createData(routeArgs, routeData);
                CmdUtils.writeMsg(context, " Zookeeper path: " + routeArgs + " route data : [" + routeData + "] Successfully.");
                handled = true;
            } else {
                CmdUtils.writeMsg(context, "set route data failed. please confirm  route configuration format is incorrect...");
                return null;
            }
        }


        //处理  zk -nodes path
        if (!CmdUtils.isEmpty(nodesArgs)) {
            logger.info("[execute] ==> zk -nodes path ...");
            List<String> nodeList = ZookeeperUtils.getChildren(nodesArgs);
            CmdUtils.writeMsg(context, nodesArgs + " list: " + nodeList.toString());
            handled = true;
        }

        handledStatus(context, handled);
        return null;
    }

    @Override
    public void plug(Context context) {

    }

    @Override
    public void unplug(Context context) {

    }


    private void handledStatus(Context context, boolean handled) {
        //没有处理  打印help info
        if (!handled) {
            CmdUtils.writeMsg(context, this.getDescriptor().getUsage());
        }
    }
}
