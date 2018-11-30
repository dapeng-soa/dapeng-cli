package com.github.dapeng.plugins.kafka.dump;

import org.apache.kafka.common.TopicPartition;
import org.clamshellcli.api.Context;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href=mailto:leihuazhe@gmail.com>maple</a>
 * @since 2018-11-29 11:27 AM
 */
public class AssignDumpConsumer extends DumpConsumer {

    public AssignDumpConsumer(DumpConfig config, Context context) {
        super(config, context);
    }

    @Override
    protected void subscribe(Context context) {
        TopicPartition topicPartition = new TopicPartition(config.getTopic(), config.getPartition());
        List<TopicPartition> topicPartitions = Collections.singletonList(topicPartition);
        consumer.assign(topicPartitions);
        if (config.getBegin() != null)
            consumer.seek(topicPartition, config.getBegin());
    }

}
