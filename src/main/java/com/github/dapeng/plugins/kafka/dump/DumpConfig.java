package com.github.dapeng.plugins.kafka.dump;

/**
 * @author <a href=mailto:leihuazhe@gmail.com>maple</a>
 * @since 2018-11-29 11:27 AM
 */
public class DumpConfig {
    /**
     * zookeeper host
     */
    private String zookeeperHost;
    /**
     * kafka host
     */
    private String broker;
    /**
     * group id
     */
    private String groupId;
    /**
     * subscribe topic
     */
    private String topic;
    /**
     * message which partition
     */
    private Integer partition;
    /**
     * topic message offset
     */
    private Long begin;
    /**
     * message size
     */
    private Long limit;

    /**
     * 只显示元信息
     */
    private boolean showInfo;

    public String getZookeeperHost() {
        return zookeeperHost;
    }

    public void setZookeeperHost(String zookeeperHost) {
        this.zookeeperHost = zookeeperHost;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getPartition() {
        return partition;
    }

    public void setPartition(Integer partition) {
        this.partition = partition;
    }

    public Long getBegin() {
        return begin;
    }

    public void setBegin(Long begin) {
        this.begin = begin;
    }

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public boolean isShowInfo() {
        return showInfo;
    }

    public void setShowInfo(boolean showInfo) {
        this.showInfo = showInfo;
    }
}
