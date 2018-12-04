package com.github.dapeng.plugins.kafka.dump;


import com.github.dapeng.plugins.kafka.DumpUtils;
import com.github.dapeng.plugins.kafka.config.MsgMetadata;
import com.github.dapeng.plugins.kafka.decode.KafkaMessageDecoder;
import com.github.dapeng.utils.CmdUtils;
import com.today.eventbus.serializer.KafkaLongDeserializer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.consumer.internals.SubscriptionState;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.clamshellcli.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href=mailto:leihuazhe@gmail.com>maple</a>
 * @since 2018-11-29 11:27 AM
 */
public abstract class DumpConsumer {
    protected static Logger log = LoggerFactory.getLogger(DefaultDumpConsumer.class);

    protected KafkaConsumer<Long, byte[]> consumer;

    private AtomicInteger counter = new AtomicInteger(0);

    private volatile boolean isRunning = true;

    private AtomicInteger readyCounter = new AtomicInteger(0);

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


    public void start() throws Exception {
        loop:
        while (isRunning) {
            ConsumerRecords<Long, byte[]> records = consumer.poll(100);
            //启动打印任务...
            processPartitionAndOffset();

            if (records != null && records.count() > 0) {
                for (ConsumerRecord<Long, byte[]> record : records) {
                    if (config.getLimit() == null) {
                        counter.incrementAndGet();
                        doConsumer(record);
                    } else if (counter.incrementAndGet() <= config.getLimit()) {
                        doConsumer(record);
                    } else {
                        break loop;
                    }
//                    没有必要提交偏移量
//                    consumer.commitAsync();
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
                log.error("consumer fetch message failed when dump some message,cause:{}", e.getMessage());
            }
            if (json == null) {
                String noDecodeValue = new String(record.value(), StandardCharsets.UTF_8);
                log.error("dump {} 消息失败，可能元信息不存在或者版本不对...", noDecodeValue);
                json = String.format("当前消息Thrift元信息不存在或版本信息不对,Dump失败。内容: %s", noDecodeValue);
            }

            String output = String.format("\n序号:%d,当前消息元数据信息:\n %s \n\n消息内容:\n %s \n \n  %s",
                    counter.get(), DumpUtils.toJson(metadata), json, DumpUtils.DIVIDING_LINE);

            log.info("\n" + output);

            CmdUtils.writeMsg(context, output);
        }
    }

    private void processPartitionAndOffset() throws Exception {
        int counter = readyCounter.incrementAndGet();
        if (counter == 2) {
            try {
                Thread.sleep(2000);
                Field subscriptions = consumer.getClass().getDeclaredField("subscriptions");
                subscriptions.setAccessible(true);
                SubscriptionState state = (SubscriptionState) subscriptions.get(consumer);
                Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap = state.allConsumed();
                StringBuilder append = new StringBuilder();
                append.append("\n分区和初始Offset信息:\n");
                topicPartitionOffsetAndMetadataMap.forEach((k, v) -> {
                    String info = String.format("\n主题: %s,分区名: %d, offset: %d\n", k.topic(), k.partition(), v.offset());
                    append.append(info);
                });
                CmdUtils.writeMsg(context, append.toString());

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void stop() {
        isRunning = false;
        consumer.close();
        log.info("DefaultDumpConsumer::stop the kafka consumer to fetch message ");
    }

    protected abstract void subscribe(Context context);
}
