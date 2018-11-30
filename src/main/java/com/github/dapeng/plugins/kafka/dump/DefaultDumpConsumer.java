package com.github.dapeng.plugins.kafka.dump;

import com.github.dapeng.utils.CmdUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.clamshellcli.api.Context;

import java.util.Collection;
import java.util.Collections;

/**
 * @author <a href=mailto:leihuazhe@gmail.com>maple</a>
 * @since 2018-11-29 11:27 AM
 */
public class DefaultDumpConsumer extends DumpConsumer {


    public DefaultDumpConsumer(DumpConfig config, Context context) {
        super(config, context);
    }

    @Override
    protected void subscribe(Context context) {
        consumer.subscribe(Collections.singletonList(config.getTopic()), new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                partitions.forEach(p -> {

                    CmdUtils.writeMsg(context, "partition:");
                    consumer.seek(p, config.getBegin());
                    log.info("Assigned partition {} to offset {}", p.partition(), config.getBegin());
                });
            }
        });
        log.info("start to analyze event,groupId:{},topic:{},begin offset:{},limit:{}",
                config.getGroupId(), config.getTopic(), config.getBegin(), config.getLimit());
    }
}
