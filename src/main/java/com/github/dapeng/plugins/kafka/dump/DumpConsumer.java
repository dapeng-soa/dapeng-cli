package com.github.dapeng.plugins.kafka.dump;


import com.github.dapeng.plugins.kafka.DumpUtils;
import com.github.dapeng.plugins.kafka.config.MsgMetadata;
import com.github.dapeng.plugins.kafka.decode.KafkaMessageDecoder;
import com.github.dapeng.utils.CmdUtils;
import com.today.eventbus.serializer.KafkaLongDeserializer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.clamshellcli.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href=mailto:leihuazhe@gmail.com>maple</a>
 * @since 2018-11-29 11:27 AM
 */
public abstract class DumpConsumer {
    protected static Logger log = LoggerFactory.getLogger(DefaultDumpConsumer.class);

    protected AtomicInteger counter = new AtomicInteger(0);

    protected KafkaConsumer<Long, byte[]> consumer;

    protected volatile boolean isRunning = true;

    //field
    protected final DumpConfig config;
    protected final Context context;

    public DumpConsumer(DumpConfig config, Context context) {
        this.config = config;
        this.context = context;
    }

    public void init() {
        Properties props = new Properties();
        props.put("bootstrap.servers", config.getBroker());
        props.put("group.id", config.getGroupId());
        //no commit to broker, unnecessary
        props.put("enable.auto.commit", "false");
        props.put("auto.commit.interval.ms", "10000");
        props.put("key.deserializer", KafkaLongDeserializer.class);
        props.put("value.deserializer", ByteArrayDeserializer.class);
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        //设置如何把byte转成object类型，例子中，通过指定string解析器，我们告诉获取到的消息的key和value只是简单个string类型。
        consumer = new KafkaConsumer<>(props);

        subscribe(context);
    }

    protected abstract void subscribe(Context context);


    public void start() {
        loop:
        while (isRunning) {
            ConsumerRecords<Long, byte[]> records = consumer.poll(100);
            for (ConsumerRecord<Long, byte[]> record : records) {
                if (config.getLimit() == null) {
                    counter.incrementAndGet();
                    doConsumer(record);
                } else if (counter.incrementAndGet() <= config.getLimit()) {
                    doConsumer(record);
                } else {
                    break loop;
                }
            }
        }
        stop();
    }


    private void doConsumer(ConsumerRecord<Long, byte[]> record) {
        //metadata
        MsgMetadata metadata = DumpUtils.buildConsumerMetadata(record);
        if (config.isShowInfo()) {
            String output = String.format("序号:%d, 消息分区:%d, offset: %d, 创建时间: %s\n",
                    counter.get(), metadata.getPartition(), metadata.getOffset(), metadata.getTimeFormat());
            CmdUtils.writeMsg(context, output);
        } else {
            String json = null;
            try {
                json = KafkaMessageDecoder.dealMessage(record.value());
            } catch (Exception e) {
                log.info("consumer fetch message failed when dump some message,cause:{}", e.getMessage());
            }
            if (json == null) {
                json = new String(record.value(), StandardCharsets.UTF_8);
            }

            String output = String.format("序号:%d,当前消息元数据信息:\n %s \n消息内容:\n %s \n",
                    counter.get(), DumpUtils.toJson(metadata), json);
            log.info("\n" + output);

            CmdUtils.writeMsg(context, output);
        }
    }

    public void stop() {
        isRunning = false;
        consumer.close();
        log.info("DefaultDumpConsumer::stop the kafka consumer to fetch message ");
    }
}
