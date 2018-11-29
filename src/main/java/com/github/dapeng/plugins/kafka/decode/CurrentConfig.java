package com.github.dapeng.plugins.kafka.decode;

import com.github.dapeng.json.OptimizedMetadata;

/**
 * @author <a href=mailto:leihuazhe@gmail.com>maple</a>
 * @since 2018-11-22 11:42 AM
 */
public class CurrentConfig {
    private String event;

    private String eventType;

    private OptimizedMetadata.OptimizedStruct eventStruct;

    private OptimizedMetadata.OptimizedService service;

    private String version;


    public CurrentConfig(String event, String eventType, OptimizedMetadata.OptimizedStruct eventStruct, OptimizedMetadata.OptimizedService service, String version) {
        this.event = event;
        this.eventType = eventType;
        this.eventStruct = eventStruct;
        this.service = service;
        this.version = version;
    }

    public String getEvent() {
        return event;
    }

    public String getEventType() {
        return eventType;
    }

    public OptimizedMetadata.OptimizedStruct getEventStruct() {
        return eventStruct;
    }

    public OptimizedMetadata.OptimizedService getService() {
        return service;
    }

    public String getVersion() {
        return version;
    }
}
