package com.github.dapeng.plugins;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterDescription;
import com.beust.jcommander.internal.Lists;
import com.github.dapeng.core.InvocationContext;
import com.github.dapeng.core.InvocationContextImpl;
import com.github.dapeng.utils.CmdEnumUtils;
import com.github.dapeng.utils.CmdUtils;
import org.clamshellcli.api.Command;
import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 去除 setting 的设置
 *
 * @author huyj
 * @Created 2018/4/20 17:09
 */
public class UnSetCmd implements Command {

    Logger logger = LoggerFactory.getLogger(UnSetCmd.class);
    private static final String NAMESPACE = "dapeng";
    private static final String CMD_NAME = "unset";
    private Descriptor cmdDescriptor;
    private static final InvocationContext invocationContext = InvocationContextImpl.Factory.getCurrentInstance();


    //参数设置
    private class CmdParams {
        @Parameter
        public List<String> parameters = Lists.newArrayList();
        @Parameter(names = {"-calleeip"}, required = false, description = "unset the invocationContext attribute CalleeIP. Usage -calleeip.")
        public boolean calleeip = false;
        @Parameter(names = {"-calleeport"}, required = false, description = "unset the invocationContext attribute CalleePort. Usage -calleeport")
        public boolean calleeport = false;
        @Parameter(names = {"-callermid"}, required = false, description = "unset the invocationContext attribute CallerMid. Usage -callermid")
        public boolean callermid = false;
        @Parameter(names = {"-callerfrom"}, required = false, description = "unset the invocationContext attribute CallerFrom. Usage -callerfrom")
        public boolean callerfrom = false;
        @Parameter(names = {"-callerip"}, required = false, description = "unset the invocationContext attribute CallerIp. Usage -callerip")
        public boolean callerip = false;
        @Parameter(names = {"-timeout"}, required = false, description = "unset the invocationContext  TimeOut. Usage -timeout")
        public boolean timeout = false;
        @Parameter(names = {"-zkhost"}, required = false, description = "unset the ZkHost . Usage -zkhost")
        public boolean zkhost = false;
    }


    //命令  使用说明
    private class CmdDescriptor implements Command.Descriptor {
        private JCommander commander;
        UnSetCmd.CmdParams cmdParams;

        public void setCommandArgs(String[] args) {
            commander = new JCommander((cmdParams = new UnSetCmd.CmdParams()), args);
        }

        @Override
        public String getNamespace() {
            return NAMESPACE;
        }

        @Override
        public String getName() {
            return CMD_NAME;
        }

        @Override
        public String getDescription() {
            return "Unset the setting attributes.";
        }

        @Override
        public String getUsage() {
            StringBuilder result = new StringBuilder();
            result.append(Configurator.VALUE_LINE_SEP)
                    .append(CMD_NAME + " [options]").append(Configurator.VALUE_LINE_SEP);

            for (Map.Entry<String, String> entry : getArguments().entrySet()) {
                result.append(String.format("%n%1$15s %2$2s %3$s", entry.getKey(), " ", entry.getValue()));
            }
            return result.toString();
            // return "unset -to timeout -ceeip callee_ip -cp callee_port -on operator_name -cf caller_from -cn customer_name -cerip caller_ip .";
        }

        @Override
        public Map<String, String> getArguments() {
            if (commander == null) commander = new JCommander(new UnSetCmd.CmdParams());
            Map<String, String> result = new HashMap<>(16);
            List<ParameterDescription> params = commander.getParameters();
            for (ParameterDescription param : params) {
                result.put(param.getNames(), param.getDescription());
            }
            return result;
        }

    }

    @Override
    public Command.Descriptor getDescriptor() {
        return (cmdDescriptor != null) ? cmdDescriptor : (cmdDescriptor = new UnSetCmd.CmdDescriptor());
    }

    @Override
    public Object execute(Context context) {
        IOConsole c = context.getIoConsole();
        String[] cmdArgs = (String[]) context.getValue(Context.KEY_COMMAND_LINE_ARGS);
        if (cmdArgs != null && cmdArgs.length > 0) {
            if (logger.isInfoEnabled()) {
                logger.info(CMD_NAME + " cmdArgs:");
                Arrays.asList(cmdArgs).forEach(item -> logger.info(item));
            }
            Arrays.asList(cmdArgs).forEach(item -> {
                if (logger.isInfoEnabled()) {
                    logger.info("remove setting attribute {}", item);
                }
                switch (CmdEnumUtils.ArgsKey.getArgEnum(item)) {
                    case SET_CALLEE_IP:
                        invocationContext.setCalleeIp(Optional.empty());
                        break;
                    case SET_CALLEE_PORT:
                        invocationContext.setCalleePort(Optional.empty());
                        break;
                    case SET_CALLER_MID:
                        invocationContext.setCallerFrom(Optional.empty());
                        break;
                    //todo
                   /* case SET_CALLER_IP:
                        ((InvocationContextImpl)invocationContext).callerIp(cmdParams.get(k));
                        break;
                    case SET_CALLER_FROM:
                        break;*/

                    case SET_TIMEOUT:
                        invocationContext.setTimeout(Optional.empty());
                        break;
                }
                CmdUtils.writeMsg(context, "the Setting of " + CmdEnumUtils.ArgsKey.getArgEnum(item).getValue() + " unset Succeed .");
            });
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
