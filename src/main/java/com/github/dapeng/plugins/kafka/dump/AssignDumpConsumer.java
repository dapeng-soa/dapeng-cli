package com.github.dapeng.plugins.kafka.dump;

import com.github.dapeng.plugins.kafka.DumpUtils;
import com.github.dapeng.plugins.kafka.config.MsgMetadata;
import com.github.dapeng.plugins.kafka.decode.KafkaMessageDecoder;
import com.github.dapeng.utils.CmdUtils;
import com.today.eventbus.serializer.KafkaLongDeserializer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.clamshellcli.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href=mailto:leihuazhe@gmail.com>maple</a>
 * @since 2018-11-29 11:27 AM
 */
public class AssignDumpConsumer extends DumpConsumer {

    public AssignDumpConsumer(DumpConfig config, Context context) {
        super(config, context);
    }

    @Override
    protected void subscribe() {
        TopicPartition topicPartition = new TopicPartition(config.getTopic(), config.getPartition());
        List<TopicPartition> topicPartitions = Collections.singletonList(topicPartition);
        consumer.assign(topicPartitions);
        consumer.seek(topicPartition, config.getBegin());
    }

}
