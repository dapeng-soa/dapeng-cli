package com.github.dapeng.plugins.kafka;

import com.github.dapeng.openapi.cache.ServiceCache;
import com.github.dapeng.plugins.kafka.config.MsgMetadata;
import com.github.dapeng.plugins.kafka.dump.DumpConfig;
import com.github.dapeng.utils.CmdUtils;
import com.github.dapeng.utils.ZookeeperUtils;
import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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
        String typeName = record.timestampType() == null ? "" : record.timestampType().name;

        return new MsgMetadata(record.key(), record.topic(), record.offset(), record.partition(),
                record.timestamp(), typeName);
    }

    /**
     * 根据 cli 的输入创建 DumpConfig 对象。
     */
    public static DumpConfig buildDumpConfig(String zkHost, String broker, String groupId,
                                             String topic, Integer partition, Long begin, Long limit, String info) {
        DumpConfig config = new DumpConfig();
        config.setZookeeperHost(zkHost);
        config.setBroker(broker);
        config.setGroupId(groupId);
        config.setTopic(topic);
        config.setPartition(partition);
        config.setBegin(begin);
        config.setLimit(limit);
        config.setShowInfo(!CmdUtils.isEmpty(info));
        return config;
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static String convertTimestamp(Long timeStamp) {
        if (timeStamp == null) {
            return "";
        }

        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timeStamp), ZoneId.of("Asia/Shanghai")).format(formatter);
    }

    public static List<String> getRuntimeService() {
        log.info("[getRuntimeService] ==>!ZookeeperUtils.isContextInitialized()=[{}]", !ZookeeperUtils.isContextInitialized());
        if (!ZookeeperUtils.isContextInitialized()) {
            ZookeeperUtils.connect();
        }
        log.info("[getRuntimeService] ==>ServiceCache.getServices()=[{}]", ServiceCache.getServices());
        List<String> services = ServiceCache.getServices().entrySet().stream().map(i -> i.getValue().getService().getNamespace() + "." + i.getKey()).collect(Collectors.toList());
        services.sort(String::compareToIgnoreCase);
        return services;
    }

    public static final String DIVIDING_LINE = "---------------------------------------------------------------------------------------------------------------------------";


}
