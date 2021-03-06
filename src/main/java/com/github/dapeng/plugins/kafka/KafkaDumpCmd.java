package com.github.dapeng.plugins.kafka;

import com.github.dapeng.plugins.kafka.dump.AssignDumpConsumer;
import com.github.dapeng.plugins.kafka.dump.DumpConfig;
import com.github.dapeng.plugins.kafka.dump.DefaultDumpConsumer;
import com.github.dapeng.plugins.kafka.dump.DumpConsumer;
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
 * Kafka 消息 dump
 */
public class KafkaDumpCmd implements Command {
    private static final Logger log = LoggerFactory.getLogger(KafkaDumpCmd.class);

    private static final String NAMESPACE = "syscmd";
    private static final String ACTION_NAME = "dump";

    @Override
    public Descriptor getDescriptor() {
        return new Descriptor() {
            public String getNamespace() {
                return NAMESPACE;
            }

            public String getName() {
                return ACTION_NAME;
            }

            public String getDescription() {
                return " dump kafka message to a json.";
            }

            public String getUsage() {
                StringBuilder sb = new StringBuilder();
                sb.append(Configurator.VALUE_LINE_SEP).append("示例:")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(Configurator.VALUE_LINE_SEP)
                        .append("1.显示最新的一条消息，以及它的 offset 、partition")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(Configurator.VALUE_LINE_SEP)
                        .append("1.1不显示指定broker，默认值 127.0.0.1:9092")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(Configurator.VALUE_LINE_SEP)
                        .append("dump -topic member_rest")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(Configurator.VALUE_LINE_SEP)
                        .append("1.2指定 broker \n")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append("dump -broker 192.168.100.1:9092 -topic member_test")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(Configurator.VALUE_LINE_SEP)
                        .append("2. 指定 offset 不显示指定 partition")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(Configurator.VALUE_LINE_SEP)
                        .append("dump -broker 127.0.0.1:9092 -topic member_test -offset 200000")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(Configurator.VALUE_LINE_SEP)
                        .append("3. 指定分区信息和 offset 信息")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(Configurator.VALUE_LINE_SEP)
                        .append("dump -broker 127.0.0.1:9092  -topic member_test  -partition 1 -offset 40")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(Configurator.VALUE_LINE_SEP)
                        .append("4.指定消费消息的范围")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(Configurator.VALUE_LINE_SEP)
                        .append("dump -broker 127.0.0.1:9092 -topic member_test -offset 20 -limit 10")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(Configurator.VALUE_LINE_SEP)
                        .append("5.通过 -info 只显示消息元信息(消息创建时间，分区，offset信息)")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(Configurator.VALUE_LINE_SEP)
                        .append("dump -broker 127.0.0.1:9092 -topic member_test -partition 1  -offset 40 -info")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(Configurator.VALUE_LINE_SEP)
                        .append("更多使用指南:")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(Configurator.VALUE_LINE_SEP)
                        .append("https://www.jianshu.com/p/763f2e257f8d")
                        .append(Configurator.VALUE_LINE_SEP);
                return sb.toString();
            }

            Map<String, String> args = null;

            public Map<String, String> getArguments() {
                if (args != null) return args;
                args = new LinkedHashMap<>();

                //zkhost
                args.put(CmdProperties.KEY_ARGS_DUMP_ZKHOST, "[required] type '-zkhost zkhost' to specific  zkhost.");
                //broker
                args.put(CmdProperties.KEY_ARGS_DUMP_BROKER, "type '-broker 127.0.0.1:9092' to specific kafka host.. ");
                //topic
                args.put(CmdProperties.KEY_ARGS_DUMP_TOPIC, "[required] type '-topic topic' to specific kafka topic.");
                //offset
                args.put(CmdProperties.KEY_ARGS_DUMP_OFFSET, "type '-offset 12' to specific kafka offset.. ");
                //partition
                args.put(CmdProperties.KEY_ARGS_DUMP_PARTITION, "type '-offset 12' to specific kafka partition.. ");
                //limit
                args.put(CmdProperties.KEY_ARGS_DUMP_LIMIT, "type '-offset 12' to specific kafka limit.. ");
                //info
                args.put(CmdProperties.KEY_ARGS_DUMP_INFO, "type '-offset 12' to specify kafka info.. ");
                return args;
            }
        };
    }

    @Override
    public Object execute(Context context) {
        Map<String, String> inputArgs = CmdUtils.getCmdArgs(context);

        String zkHost = inputArgs.get(CmdProperties.KEY_ARGS_DUMP_ZKHOST);
        String broker = inputArgs.get(CmdProperties.KEY_ARGS_DUMP_BROKER);

        String topic = inputArgs.get(CmdProperties.KEY_ARGS_DUMP_TOPIC);
        String partition = inputArgs.get(CmdProperties.KEY_ARGS_DUMP_PARTITION);
        String offset = inputArgs.get(CmdProperties.KEY_ARGS_DUMP_OFFSET);
        String limit = inputArgs.get(CmdProperties.KEY_ARGS_DUMP_LIMIT);
        String info = inputArgs.get(CmdProperties.KEY_ARGS_DUMP_INFO);

        CmdUtils.writeMsg(context, String.format("inputParams: topic:%s, partition:%s, offset:%s, limit:%s, info:%s, broker:%s",
                topic, partition, offset, limit, info, broker));

        //3. validate kafka dump some args.
        if (CmdUtils.isEmpty(topic)) {
            CmdUtils.writeMsg(context, "dump kafka 消息时 topic 不能为空");
            String usage = getDescriptor().getUsage();
            CmdUtils.writeMsg(context, usage);
            return null;
        }

        Integer partitionInt = CmdUtils.isEmpty(partition) ? null : Integer.valueOf(partition);

        Long offsetLong = CmdUtils.isEmpty(offset) ? null : Long.valueOf(offset);
        Long limitLong = CmdUtils.isEmpty(limit) ? null : Long.valueOf(limit);

        if (CmdUtils.isEmpty(broker)) {
            CmdUtils.writeMsg(context, "kafka broker host is not set, use default 127.0.0.1:9092");
            broker = "127.0.0.1:9092";
        }

        DumpConfig config = DumpUtils.buildDumpConfig(zkHost, broker, "TEST-GROUP", topic,
                partitionInt, offsetLong, limitLong, info);

        DumpConsumer dumpConsumer;
        if (config.getPartition() != null) {
            dumpConsumer = new AssignDumpConsumer(config, context);
        } else {
            dumpConsumer = new DefaultDumpConsumer(config, context);
        }
        //fetch metadata
        fetchMetadata();

        try {
            dumpConsumer.init();
            dumpConsumer.start();
        } catch (Throwable ex) {
            log.error(ex.getMessage(), ex);
            dumpConsumer.stop();
        }
        //


        return null;
    }

    private void fetchMetadata() {
        //************************【获取服务列表 service -list】****************
        List<String> services = DumpUtils.getRuntimeService();
        log.info("[execute] ==>services=[{}]", services);
    }

    @Override
    public void plug(Context context) {

    }

    @Override
    public void unplug(Context context) {

    }
}
