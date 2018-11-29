package com.github.dapeng.plugins.kafka;

import com.github.dapeng.plugins.kafka.dump.DumpConfig;
import com.github.dapeng.plugins.kafka.dump.KafkaDumpConsumer;
import com.github.dapeng.utils.CmdProperties;
import com.github.dapeng.utils.CmdUtils;
import org.clamshellcli.api.Command;
import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
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
                sb.append(Configurator.VALUE_LINE_SEP)
                        .append(" dump -topic member_test   ")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(" dump -topic member_test -partition 1 -offset 20  ")
                        .append(Configurator.VALUE_LINE_SEP);

                return sb.toString();
            }

            Map<String, String> args = null;

            public Map<String, String> getArguments() {
                if (args != null) return args;
                args = new LinkedHashMap<>();

                //zkhost
                args.put(CmdProperties.KEY_ARGS_DUMP_ZKHOST, "[required] type '-zkhost zkhost' to specific  zkhost.");
                //kafkahost
                args.put(CmdProperties.KEY_ARGS_DUMP_KAFKA_HOST, "type '-kafkahost kafkahost' to specific kafka host.. ");


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
                //broker
                args.put(CmdProperties.KEY_ARGS_DUMP_BROKER, "type '-offset 12' to specific kafka broker.. ");
                return args;
            }
        };
    }

    @Override
    public Object execute(Context context) {
        Map<String, String> inputArgs = CmdUtils.getCmdArgs(context);

        String zkHost = inputArgs.get(CmdProperties.KEY_ARGS_DUMP_ZKHOST);
        String kafkaHost = inputArgs.get(CmdProperties.KEY_ARGS_DUMP_KAFKA_HOST);

        String topic = inputArgs.get(CmdProperties.KEY_ARGS_DUMP_TOPIC);
        String partition = inputArgs.get(CmdProperties.KEY_ARGS_DUMP_PARTITION);
        String offset = inputArgs.get(CmdProperties.KEY_ARGS_DUMP_OFFSET);
        String limit = inputArgs.get(CmdProperties.KEY_ARGS_DUMP_LIMIT);
        String info = inputArgs.get(CmdProperties.KEY_ARGS_DUMP_INFO);
        String broker = inputArgs.get(CmdProperties.KEY_ARGS_DUMP_BROKER);

        CmdUtils.writeMsg(context, String.format("inputParams: topic:%s, partition:%s, offset:%s, limit:%s, info:%s, broker:%s",
                topic, partition, offset, limit, info, broker));

        DumpConfig config = DumpUtils.buildDumpConfig(zkHost, kafkaHost, "TEST-GROUP", topic,
                Integer.valueOf(partition), Long.valueOf(offset), Long.valueOf(limit));

        KafkaDumpConsumer kafkaDumpConsumer = new KafkaDumpConsumer(config, context);

        try {
            kafkaDumpConsumer.init();
            kafkaDumpConsumer.start();
        } catch (Throwable ex) {
            log.error(ex.getMessage(), ex);
            kafkaDumpConsumer.stop();
        }
        //

        //3. validate kafka dump some args.
        if (CmdUtils.isEmpty(topic) || CmdUtils.isEmpty(partition) || CmdUtils.isEmpty(offset)) {
            CmdUtils.writeMsg(context, " request format is invalid.. please check your input.....");
            String usage = getDescriptor().getUsage();
            CmdUtils.writeMsg(context, usage);
        } else {
//            String jsonRequestSample = ServiceUtils.getJsonRequestSample(sName, sVersion, sMethod);
//            if (!CmdUtils.isEmpty(file_out)) {
//                ServiceUtils.writerFile(context, file_out, jsonRequestSample);
//                //CmdUtils.writeMsg(context, file_out + "is generated . ");
//            } else {
//                CmdUtils.writeMsg(context, CmdUtils.getResult(jsonRequestSample));
//            }

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
