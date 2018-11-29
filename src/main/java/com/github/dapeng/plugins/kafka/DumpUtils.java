package com.github.dapeng.plugins.kafka;

import com.github.dapeng.plugins.kafka.config.MsgMetadata;
import com.github.dapeng.plugins.kafka.dump.DumpConfig;
import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.record.TimestampType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author <a href=mailto:leihuazhe@gmail.com>maple</a>
 * @since 2018-11-29 1:53 PM
 */
public class DumpUtils {
    private static Logger log = LoggerFactory.getLogger(DumpUtils.class);
    private static Gson gson = new Gson();

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss SSS");

    /**
     * 根据每条消息元信息记录构造该条消息的元数据信息对象 {@link MsgMetadata}
     */
    public static MsgMetadata buildConsumerMetadata(ConsumerRecord<Long, byte[]> record) {
        String typeName;
        TimestampType timestampType = record.timestampType();

        if (timestampType == null) {
            typeName = "";
        } else {
            typeName = timestampType.name;
        }
        log.info("record info: {}  ==> timestampType: {} ", record.toString(), typeName);





        return new MsgMetadata(record.key(), record.topic(), record.offset(), record.partition(),
                record.timestamp(), typeName);
    }

    /**
     * 根据 cli 的输入创建 DumpConfig 对象。
     */
    public static DumpConfig buildDumpConfig(String zkHost, String kafkaHost, String groupId,
                                             String topic, Integer partition, Long begin, Long limit) {
        DumpConfig config = new DumpConfig();
        config.setZookeeperHost(zkHost);
        config.setKafkaHost(kafkaHost);
        config.setGroupId(groupId);
        config.setTopic(topic);
        config.setPartition(partition);
        config.setBegin(begin);
        config.setLimit(limit);

        return config;
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static String convertTimestamp(Long timeStamp) {
        if (timeStamp == null) {
            return "";
        }
        return LocalDateTime.now(ZoneId.of("Asia/Shanghai")).format(formatter);
    }


}
