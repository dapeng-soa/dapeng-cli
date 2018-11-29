package com.github.dapeng.plugins.kafka.config;

import com.github.dapeng.plugins.kafka.DumpUtils;

/**
 * @author <a href=mailto:leihuazhe@gmail.com>maple</a>
 * @since 2018-11-23 6:05 PM
 */
public class MsgMetadata {
    private final Long key;
    private final String topic;
    private final Long offset;
    private final Integer partition;
    private final Long timestamp;
    private final String timeFormat;
    private final String timestampType;

    public MsgMetadata(Long key, String topic, Long offset, Integer partition, Long timestamp, String timestampType) {
        this.key = key;
        this.topic = topic;
        this.offset = offset;
        this.partition = partition;
        this.timestamp = timestamp;
        this.timeFormat = DumpUtils.convertTimestamp(timestamp);
        this.timestampType = timestampType;
    }

    public Long getKey() {
        return key;
    }

    public String getTopic() {
        return topic;
    }

    public Long getOffset() {
        return offset;
    }

    public Integer getPartition() {
        return partition;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public String getTimestampType() {
        return timestampType;
    }

    @Override
    public String toString() {
        return "当前消息元信息:[" + "key=" + key + ", topic='" + topic + '\'' + ", offset=" + offset + ", partition=" + partition +
                ", timestamp=" + timestamp + ", timeFormat='" + timeFormat +
                '\'' + ", timestampType='" + timestampType + '\'' /*+ ", 消息内容: " + content*/ + " ]";
    }
}
