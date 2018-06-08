package com.github.dapeng.plugins;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterDescription;
import com.beust.jcommander.internal.Lists;
import com.github.dapeng.core.InvocationContext;
import com.github.dapeng.core.InvocationContextImpl;
import com.github.dapeng.core.enums.CodecProtocol;
import com.github.dapeng.core.enums.LoadBalanceStrategy;
import com.github.dapeng.core.helper.IPUtils;
import com.github.dapeng.utils.CmdEnumUtils;
import com.github.dapeng.utils.CmdUtils;
import com.github.dapeng.utils.ZookeeperUtils;
import org.clamshellcli.api.Command;
import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 设置 setting 信息
 *
 * @author huyj
 * @Created 2018/4/19 19:28
 */
public class SetCmd implements Command {

    private static Logger logger = LoggerFactory.getLogger(SetCmd.class);
    private static final String NAMESPACE = "dapeng";
    private static final String CMD_NAME = "set";
    private CmdDescriptor cmdDescriptor;
    public static final InvocationContext invocationContext = InvocationContextImpl.Factory.currentInstance();


    //参数设置
    private class CmdParams {
        @Parameter
        public List<String> parameters = Lists.newArrayList();

        @Parameter(names = {"-calleeip"}, required = false, description = "set the invocationContext attribute CalleeIP. Usage -calleeip.")
        public boolean calleeip = false;
        @Parameter(names = {"-calleeport"}, required = false, description = "set the invocationContext attribute CalleePort. Usage -calleeport.")
        public boolean calleeport = false;
        @Parameter(names = {"-callermid"}, required = false, description = "set the invocationContext attribute CallerMid. Usage -callermid.")
        public boolean callermid = false;
        @Parameter(names = {"-callerfrom"}, required = false, description = "set the invocationContext attribute CallerFrom. Usage -callerfrom.")
        public boolean callerfrom = false;
        @Parameter(names = {"-callerip"}, required = false, description = "set the invocationContext attribute CallerIp. Usage -callerip.")
        public boolean callerip = false;
        @Parameter(names = {"-timeout"}, required = false, description = "set the invocationContext  TimeOut. Usage -timeout.")
        public boolean timeout = false;
        @Parameter(names = {"-zkhost"}, required = false, description = "set the ZkHost . Usage -zkhost.")
        public boolean zkhost = false;
        @Parameter(names = {"-cookie"}, required = false, description = "set the cookie . Usage -cookie.")
        public boolean cookie = false;

        @Parameter(names = {"-sessiontid"}, required = false, description = "set the sessionTid . Usage -sessiontid.")
        public boolean sessiontid = false;
        @Parameter(names = {"-userid"}, required = false, description = "set the userId . Usage -userid.")
        public boolean userid = false;
        @Parameter(names = {"-userip"}, required = false, description = "set the userIp . Usage -userip.")
        public boolean userip = false;
        @Parameter(names = {"-transactionid"}, required = false, description = "set the transactionId . Usage -transactionid.")
        public boolean transactionid = false;
        @Parameter(names = {"-transactionsequence"}, required = false, description = "set the transactionSequence . Usage -transactionsequence.")
        public boolean transactionsequence = false;
        @Parameter(names = {"-callertid"}, required = false, description = "set the callerTid . Usage -callertid.")
        public boolean callertid = false;
        @Parameter(names = {"-operatorid"}, required = false, description = "set the operatorId . Usage -operatorid.")
        public boolean operatorid = false;
        @Parameter(names = {"-loadbalancestrategy"}, required = false, description = "set the loadBalanceStrategy . Usage -loadbalancestrategy.")
        public boolean loadbalancestrategy = false;
        @Parameter(names = {"-codecprotocol"}, required = false, description = "set the codeCprotocol . Usage -codecprotocol.")
        public boolean codecprotocol = false;
       /* @Parameter(names = {"-seqid"}, required = false, description = "set the seqId . Usage -seqid.")
        public boolean seqid = false;*/
    }


    //命令  使用说明
    private class CmdDescriptor implements Command.Descriptor {
        private JCommander commander;
        CmdParams cmdParams;

        public void setCommandArgs(String[] args) {
            commander = new JCommander((cmdParams = new CmdParams()), args);
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
            return " Set the dapeng setting attributes.";
        }

        @Override
        public String getUsage() {
            StringBuilder result = new StringBuilder();
            result
                    .append(Configurator.VALUE_LINE_SEP)
                    .append(CMD_NAME + " [options]").append(Configurator.VALUE_LINE_SEP);
            for (Map.Entry<String, String> entry : getArguments().entrySet()) {
                result.append(String.format("%n%1$15s %2$2s %3$s", entry.getKey(), " ", entry.getValue()));
            }
            return result.toString();
        }

        @Override
        public Map<String, String> getArguments() {
            if (commander == null) commander = new JCommander(new CmdParams());
            Map<String, String> result = new HashMap<String, String>();
            List<ParameterDescription> params = commander.getParameters();
            for (ParameterDescription param : params) {
                result.put(param.getNames(), param.getDescription());
            }
            return result;
        }

    }

    @Override
    public Command.Descriptor getDescriptor() {
        return (cmdDescriptor != null) ? cmdDescriptor : (cmdDescriptor = new CmdDescriptor());
    }

    @Override
    public Object execute(Context context) {
        logger.info("[execute] ==> set command execute....");
        Map<String, String> cmdParams = CmdUtils.getCmdArgs(context);

        logger.info("[execute] ==> cmdParams=[{}]", cmdParams);
        if (cmdParams.isEmpty()) {
            StringBuilder info = new StringBuilder(128);
            info.append("the setting info is:").append("\n");
            info.append("-- the Timeout: ").append(invocationContext.timeout().orElse(null)).append("\n");
            info.append("-- the CalleeIp: ").append(invocationContext.calleeIp().orElse(null)).append("\n");
            info.append("-- the CalleePort: ").append(invocationContext.calleePort().orElse(null)).append("\n");
            info.append("-- the CallerMid: ").append(invocationContext.callerMid().orElse(null)).append("\n");
            /*info.append("-- the CallerFrom: ").append(invocationContext.callerFrom().orElse(null)).append("\n");
            info.append("-- the CallerIp: ").append(invocationContext.call().orElse(null)).append("\n");*/
            info.append("-- the zkHost: ").append(ZookeeperUtils.getZkHost()).append("\n");
            info.append("-- the cookie: ").append(invocationContext.cookies()).append("\n");

            info.append("-- the sessionTid: ").append(invocationContext.sessionTid().orElse(null)).append("\n");
            info.append("-- the userId: ").append(invocationContext.userId().orElse(null)).append("\n");
            info.append("-- the userIp: ").append(invocationContext.userIp().orElse(null)).append("\n");
            //info.append("transactionId: ").append(invocationContext.transactionId.orElse(null)).append("\n");
            //info.append("transactionSequence: ").append(invocationContext.transactionSequence.orElse(null)).append("\n");
            info.append("-- the callerTid: ").append(invocationContext.callerTid()).append("\n");
            info.append("-- the operatorId: ").append(invocationContext.operatorId().orElse(null)).append("\n");
            info.append("-- the loadBalanceStrategy: ").append(invocationContext.loadBalanceStrategy().orElse(null)).append("\n");
            //info.append("-- the seqId").append(Objects.isNull(invocationContext.seqId()) ? null : invocationContext.seqId()).append("\n");

            CmdUtils.writeMsg(context, info.toString());
            return null;
        }

        cmdParams.forEach((String k, String v) -> {
            if (logger.isInfoEnabled()) {
                logger.info("argKey:{}  argValue:{}", k, v);
            }
            switch (CmdEnumUtils.ArgsKey.getArgEnum(k)) {
                case SET_CALLEE_IP:
                    //nvocationContext.calleeIp(cmdParams.get(k));
                    invocationContext.calleeIp(IPUtils.transferIp(cmdParams.get(k)));
                    break;
                case SET_CALLEE_PORT:
                    invocationContext.calleePort(Integer.parseInt(cmdParams.get(k)));
                    break;
                case SET_CALLER_MID:
                    invocationContext.callerMid(cmdParams.get(k));
                    break;
                //todo
               /* case SET_CALLER_IP:
                    ((InvocationContextImpl)invocationContext).callerIp(cmdParams.get(k));
                    break;
                case SET_CALLER_FROM:
                    break;*/

                case SET_TIMEOUT:
                    invocationContext.timeout(Integer.valueOf(cmdParams.get(k)));
                    break;
                case SET_COOKIE:
                    String args[] = cmdParams.get(k).split(":");
                    logger.info("args[]: {}", args.toString());
                    invocationContext.setCookie(args[0], args[1]);
                    break;
                case SET_ZKHOST:
                    String zkHost = cmdParams.get(k);
                    if (zkHost.equalsIgnoreCase(ZookeeperUtils.getZkHost())) {
                        CmdUtils.writeMsg(context, "the ZK have been connected on " + ZookeeperUtils.getZkHost());
                    } else {
                        CmdUtils.writeMsg(context, "set zkHost and waiting for reset Zkconnection " + zkHost);
                        ZookeeperUtils.setZkHost(cmdParams.get(k));
                        /*ServiceUtils.destroyZk();*/
                        /* ServiceUtils.iniContext();*/
                    }
                    break;

                case SET_SESSIONTID:
                    invocationContext.sessionTid((cmdParams.get(k)));
                    break;

                case SET_USERID:
                    invocationContext.userId((Long.parseLong(cmdParams.get(k))));
                    break;
                case SET_USERIP:
                    invocationContext.userIp(IPUtils.transferIp(cmdParams.get(k)));
                    break;
                case SET_CALLERTID:
                    invocationContext.callerTid(cmdParams.get(k));
                    break;
                case SET_OPERATORID:
                    invocationContext.operatorId(Long.parseLong(cmdParams.get(k)));
                    break;
                case SET_LOADBALANCESTRATEGY:
                    invocationContext.loadBalanceStrategy(LoadBalanceStrategy.findByValue(cmdParams.get(k)));
                    break;
                case SET_CODECPROTOCOL:
                    invocationContext.codecProtocol(CodecProtocol.toCodecProtocol((byte) Integer.parseInt(cmdParams.get(k))));
                    break;
               /* case SET_SEQID:
                    ((InvocationContextImpl) invocationContext).seqId(Integer.parseInt(cmdParams.get(k)));
                    break;*/
            }
            CmdUtils.writeMsg(context, "setting of  " + CmdEnumUtils.ArgsKey.getArgEnum(k).getValue() + " set Succeed .");
        });
        return null;
    }

    @Override
    public void plug(Context context) {

    }

    @Override
    public void unplug(Context context) {

    }
}
