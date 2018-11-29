package com.github.dapeng.plugins.kafka.dump;

import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href=mailto:leihuazhe@gmail.com>maple</a>
 * @since 2018-11-29 11:27 AM
 */
public abstract class AbstractConsumer {

    protected AtomicInteger counter = new AtomicInteger(0);
    protected final DumpConfig config;

    protected AbstractConsumer(DumpConfig config) {
        this.config = config;
    }


    /**
     * 配置
     */
    protected Properties configConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", config.getKafkaHost());
        props.put("group.id", config.getKey());
        //no commit to broker, unnecessary
        props.put("enable.auto.commit", "false");
        props.put("auto.commit.interval.ms", "10000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.LongDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        return props;
    }

    public abstract void start();
}
