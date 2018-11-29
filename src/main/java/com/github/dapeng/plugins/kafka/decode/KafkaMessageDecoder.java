package com.github.dapeng.plugins.kafka.decode;

import com.github.dapeng.core.SoaException;
import com.github.dapeng.json.JsonSerializer;
import com.github.dapeng.json.OptimizedMetadata;
import com.github.dapeng.openapi.cache.ServiceCache;
import com.github.dapeng.org.apache.thrift.TException;
import com.github.dapeng.org.apache.thrift.protocol.TCompactProtocol;
import com.github.dapeng.util.TCommonTransport;
import com.github.dapeng.util.TKafkaTransport;
import com.today.eventbus.serializer.KafkaMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Desc: KafkaMessageDecoder
 *
 * @author hz.lei
 * @date 2018年05月16日 下午10:12
 */
public class KafkaMessageDecoder {
    private static Logger log = LoggerFactory.getLogger(KafkaMessageDecoder.class);
    private static Pattern pattern = Pattern.compile("\\S(scala)\\S");

    /**
     * 将事件解码为 json 形式
     */
    public static String dealMessage(byte[] value) throws TException {
        KafkaMessageProcessor processor = new KafkaMessageProcessor();
        String eventType;
        try {
            eventType = processor.getEventType(value);
        } catch (Exception e) {
            log.error("[RestKafkaConsumer]:解析消息eventType出错，忽略该消息: " + e.getMessage(), e);
            return null;
        }
        byte[] eventBinary = processor.getEventBinary();
        CurrentConfig config = fetchMetadata(eventType);

        JsonSerializer jsonDecoder = new JsonSerializer(config.getService(), null, config.getVersion(), config.getEventStruct());
        String body = jsonDecoder.read(new TCompactProtocol(new TKafkaTransport(eventBinary, TCommonTransport.Type.Read)));
        log.info("event body: {}", body);
        return body;
    }

    /**
     * fetchMetadata
     */
    private static CurrentConfig fetchMetadata(String eventType) throws SoaException {
        //event
        String event = convertType(eventType);
        //serviceMap
        Map<String, OptimizedMetadata.OptimizedService> serviceMap = getServiceMap();
        OptimizedMetadata.OptimizedStruct struct = null;
        for (OptimizedMetadata.OptimizedService service : serviceMap.values()) {
            struct = service.getOptimizedStructs().get(event);
            if (struct != null) {
                return new CurrentConfig(event, eventType, struct, service, service.getService().meta.version);
            }
        }
        throw new SoaException("Error-dump-001", "当前zk环境没有发现event元数据信息，无法解码消息");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, OptimizedMetadata.OptimizedService> getServiceMap() {
        try {
            Field serviceMap = ServiceCache.class.getDeclaredField("services");
            serviceMap.setAccessible(true);
            Map<String, OptimizedMetadata.OptimizedService> services = (Map<String, OptimizedMetadata.OptimizedService>) serviceMap.get(null);
            return services;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;

    }


    /**
     * convert event
     */
    private static String convertType(String eventType) {
        String event = eventType.replaceAll(".scala", "");
        Matcher matcher = pattern.matcher(eventType);
        if (matcher.matches()) {
            String group = matcher.group(0);
            System.out.println(group);
        }
        return event;
    }
}
