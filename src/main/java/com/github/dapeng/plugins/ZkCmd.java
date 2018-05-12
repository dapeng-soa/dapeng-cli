package com.github.dapeng.plugins;

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
                        .append(Configurator.VALUE_LINE_SEP);
                return sb.toString();
            }

            Map<String, String> args = null;

            public Map<String, String> getArguments() {
                if (args != null) return args;
                args = new LinkedHashMap<String, String>();
                args.put(CmdProperties.KEY_ARGS_ZK_GET, "type '-get' to get zk node data by path.");
                args.put(CmdProperties.KEY_ARGS_ZK_SET, "type '-set' to set zk node data by path. Note: '/soa/runtime/services' subPath can not be setting! ");
                args.put(CmdProperties.KEY_ARGS_ZK_NODE, "type '-nodes' to get path child nodes.");
                args.put(CmdProperties.KEY_ARGS_DATA, "type ' -d ' the data will be set, like: '-set path -d data.'");
                args.put(CmdProperties.KEY_ARGS_FILE_READ, "type '-f' to get zkData content for file.");
                args.put(CmdProperties.KEY_ARGS_FILE_OUT, "type '-o' to save the data to file.");

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
        String args_get = inputArgs.get(CmdProperties.KEY_ARGS_ZK_GET);
        String args_set = inputArgs.get(CmdProperties.KEY_ARGS_ZK_SET);
        String args_data = inputArgs.get(CmdProperties.KEY_ARGS_DATA);
        String args_nodes = inputArgs.get(CmdProperties.KEY_ARGS_ZK_NODE);
        String file_read = inputArgs.get(CmdProperties.KEY_ARGS_FILE_READ);
        String file_out = inputArgs.get(CmdProperties.KEY_ARGS_FILE_OUT);

        boolean handled = false;
        //处理  zk -get path
        if (!CmdUtils.isEmpty(args_get)) {
            logger.info("[execute] ==> handle  zk -get path ...");
            String data = ZookeeperUtils.getData(args_get);

            //-o
            if (!CmdUtils.isEmpty(file_out)) {
                ServiceUtils.writerFile(context, file_out, data);
            } else {
                CmdUtils.writeMsg(context, "the zk path[" + args_get + "] data is :" + data);
            }
            handled = true;
        }

        //处理  zk -set path -d | -f
        if (!CmdUtils.isEmpty(args_set)) {
            logger.info("[execute] ==> handle  zk -set path -d data ...");
            String setData = null;
            if (!CmdUtils.isEmpty(file_read)) setData = ServiceUtils.readFromeFile(file_read);
            if (!CmdUtils.isEmpty(args_data)) setData = args_data;

            ZookeeperUtils.setData(context, args_set, setData);
            handled = true;
        }

        //处理  zk -nodes path
        if (!CmdUtils.isEmpty(args_nodes)) {
            logger.info("[execute] ==> zk -nodes path ...");
            List<String> nodeList = ZookeeperUtils.getChildren(args_nodes);

            //-o
            if (!CmdUtils.isEmpty(file_out)) {
                ServiceUtils.writerFile(context, file_out, CmdUtils.getResult(nodeList));
            } else {
                CmdUtils.writeMsg(context, "the path " + args_nodes + " child nodes is :" + CmdUtils.getResult(nodeList));
                handled = true;
            }

        }

        CmdUtils.handledStatus(context, handled, this.getDescriptor().getUsage());
        return null;
    }

    @Override
    public void plug(Context context) {

    }

    @Override
    public void unplug(Context context) {

    }
}
