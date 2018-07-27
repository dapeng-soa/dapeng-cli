package com.github.dapeng.utils;

import com.github.dapeng.core.metadata.Method;
import com.github.dapeng.core.metadata.Service;
import com.github.dapeng.json.JsonSerializer;
import com.github.dapeng.org.apache.thrift.TException;
import com.github.dapeng.util.SoaMessageParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.StringReader;
import java.util.stream.Collectors;

public class JsonSerializerUtils {
    private static final Logger logger = LoggerFactory.getLogger(ServiceUtils.class);

    public static void main(String[] args) {
        String serviceName = "com.today.api.category.service.OpenCategoryService";
        String methodName = "listCategoryDetailBySkuNos";
        String version = "1.0.0";
        String hexStr = "000000c50201010000000a0b000100000032636f6d2e746f6461792e6170692e63617465676f72792e736572766963652e4f70656e43617465676f7279536572766963650b00020000001a6c69737443617465676f727944657461696c4279536b754e6f730b000300000005312e302e300b00040000000a4a736f6e43616c6c65720800050a004b010a000934b84b0100000b3f0d00170b0b000000000019f88480000832303432363635310832303435343336340834313032303738390837313032303030380003";
        toJson(serviceName, methodName, version, hexStr, "request");
//        requestBinaryToJson(serviceName, methodName, version, hexStr, "response");
        logger.info("***********");
        System.out.println("***********");
        //System.exit(-1);
    }


    public static String toJson(String serviceName, String methodName, String version, String hexStr, String parseType) {

        String serviceXml = ServiceUtils.getMetadata(serviceName, version);
        Service service = JAXB.unmarshal(new StringReader(serviceXml), Service.class);
        Method serviceMethod = service.methods.stream().filter(method -> method.name.equals(methodName)).collect(Collectors.toList()).get(0);
        final ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer(8192);
        byte[] bytes = hexStr2bytes(hexStr);
        byteBuf.setBytes(0, bytes);
        byteBuf.writerIndex(bytes.length);

        // System.out.println(dumpToStr(requestBuf));
        JsonSerializer jsonDecoder;
        if (parseType.equalsIgnoreCase("request")) {
            jsonDecoder = new JsonSerializer(service, serviceMethod, version, serviceMethod.request);
        } else {
            jsonDecoder = new JsonSerializer(service, serviceMethod, version, serviceMethod.response);
        }
        SoaMessageParser<String> parser = new SoaMessageParser<>(byteBuf, jsonDecoder);
        try {
            parser.parseHeader();
            parser.parseBody();
        } catch (TException e) {
            e.printStackTrace();
            logger.error("SoaMessageParser parse info Error", e);
        }
        //System.out.println(parser.getBody());
        byteBuf.release();
        return parser.getBody();
    }


    /**
     * transfer hex string to bytes
     *
     * @param hex
     * @return
     */
    public static byte[] hexStr2bytes(String hex) {
        int length = hex.length() / 2;
        // must be multiple of 2
        assert hex.length() % 2 == 0;
        byte[] result = new byte[length];
        for (int i = 0, j = 0; j < length; j += 2, i += 4) {
            if (i + 4 > hex.length()) {
                String _2bytes = hex.substring(i, i + 2) + "00";
                int anInt = Integer.parseInt(_2bytes, 16);
                result[j] = (byte) ((anInt >> 8) & 0xff);
            } else {
                String _2bytes = hex.substring(i, i + 4);
                int anInt = Integer.parseInt(_2bytes, 16);
                result[j] = (byte) ((anInt >> 8) & 0xff);
                result[j + 1] = (byte) (anInt & 0xff);
            }
        }
        return result;
    }
}
