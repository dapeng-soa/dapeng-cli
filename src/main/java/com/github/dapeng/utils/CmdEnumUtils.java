package com.github.dapeng.utils;

/**
 * 命令行 工具枚举
 *
 * @author huyj
 * @Created 2018/4/19 20:16
 */
public class CmdEnumUtils {

    public interface CmdArg {
        String getKey();

        Object getValue();

        Object getValueByKey(String key);
        //static ArgsKey getArgEnum(String key);
    }

    public enum ArgsKey implements CmdArg {

        DEFAULT("Unknown Key", "Unknown Value"),

        //dapeng  setting,
        SET_ZKHOST("-zkhost", "ZkHost"),

        SET_CALLEE_IP("-calleeip", "CalleeIp"),
        SET_CALLEE_PORT("-calleeport", "CalleePort"),
        SET_CALLER_MID("-callermid", "CallerMid"),
        SET_CALLER_IP("-callerip", "CallerIp"),
        SET_CALLER_FROM("-callerfrom", "CallerFrom"),
        SET_TIMEOUT("-timeout", "TimeOut"),
        SET_COOKIE("-cookie", "cookie"),

        //2018/06/18 add
        SET_SESSIONTID("-sessiontid", "sessionTid"),
        SET_USERID("-userid", "userId"),
        SET_USERIP("-userip", "userIp"),
        //SET_TRANSACTIONID("-transactionid", "transactionId"),
        //SET_TRANSACTIONSEQUENCE("-transactionsequence", "transactionSequence"),
        SET_CALLERTID("-callertid", "callerTid"),
        SET_OPERATORID("-operatorid", "operatorId"),
        SET_LOADBALANCESTRATEGY("-loadbalancestrategy", "loadBalanceStrategy"),
        SET_CODECPROTOCOL("-codecprotocol", "codecProtocol");
        //SET_SEQID("-seqid", "seqId");

        private String key;//参数key
        private Object value;//参数值

        // 构造方法
        ArgsKey(String key, String value) {
            this.key = key;
            this.value = value;
        }

        //接口方法
        @Override
        public String getKey() {
            return this.key;
        }

        //接口方法
        @Override
        public Object getValue() {
            return this.value;
        }

        //接口方法
        @Override
        public Object getValueByKey(String key) {
            for (ArgsKey args : ArgsKey.values()) {
                if (args.key.equalsIgnoreCase(key)) {
                    return args.value;
                }
            }
            return DEFAULT.value;
        }

        //接口方法
        public static ArgsKey getArgEnum(String key) {
            for (ArgsKey args : ArgsKey.values()) {
                if (args.key.equalsIgnoreCase(key)) {
                    return args;
                }
            }
            return DEFAULT;
        }

    }
}
