package com.github.dapeng.utils;

import com.github.dapeng.client.netty.JsonPost;
import com.github.dapeng.core.InvocationContextImpl;
import com.github.dapeng.core.SoaCode;
import com.github.dapeng.core.SoaException;
import com.github.dapeng.core.enums.CodecProtocol;
import com.github.dapeng.core.metadata.*;
import com.github.dapeng.json.OptimizedMetadata;
import com.github.dapeng.metadata.MetadataClient;
import com.github.dapeng.openapi.cache.ServiceCache;
import com.github.dapeng.plugins.SetCmd;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.clamshellcli.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.github.dapeng.utils.CmdProperties.OPEN_API_TIMEOUT;

public class ServiceUtils {
    private static final Logger logger = LoggerFactory.getLogger(ServiceUtils.class);

    public static String getMetadata(String serviceName, String version) {
        MetadataClient client = new MetadataClient(serviceName, version);
        String data = "";
        try {
            data = client.getServiceMetadata();
        } catch (Exception e) {
            data = " Failed to get metadata........" + e.getMessage();
        }
        return data;
    }

    public static void writerFile(Context context, String fileName, String content) {
        FileWriter fw = null;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                // 先得到文件的上级目录，并创建上级目录，在创建文件
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            fw = new FileWriter(file);
            fw.write(content);
            fw.flush();
        } catch (Exception e) {
            CmdUtils.writeMsg(context, " Failed to written file[" + fileName + "] cause:" + e.getMessage());
        } finally {
            try {
                fw.close();
            } catch (Exception e) {
                CmdUtils.writeMsg(context, " Failed to close file[" + fileName + "] cause:" + e.getMessage());
            }
        }
        CmdUtils.writeMsg(context, "the data has been saved to file[" + fileName + "] succeed");
    }

    public static String getJsonRequestSample(String serviceName, String version, String methodName) {
        if (!ZookeeperUtils.isContextInitialized()) {
            ZookeeperUtils.connect();
        }

        Service service = getService(serviceName, version);

        // System.out.println(" ServiceCache: " + ServiceCache.getServices());
        Struct struct = getMethod(service, methodName);

        if (struct == null) {
            return methodName + " of " + serviceName + ":" + version + " not found..........";
        } else {
            List<Field> parameters = struct.getFields();
            Map<String, Object> map = new HashMap<>();
            map.put("body", getSample(service, parameters));
            return gson_format.toJson(map);
        }
    }

    public final static Gson gson_format = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getName().equals("attachment") || f.getName().equals("__isset_bitfield");
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return clazz == ByteBuffer.class;
        }
    }).setPrettyPrinting().create();

    private static Service getService(String serviceName, String versionName) {

        Service service = ServiceCache.getService(serviceName, versionName);
        return service;
    }

    private static Struct getMethod(Service service, String methodName) {
        List<Method> methods = service.getMethods();

        Optional<Method> method = methods.stream().filter(i -> i.name.equals(methodName)).findFirst();

        return method.isPresent() ? method.get().getRequest() : null;
    }

    private static Map<String, Object> getSample(Service service, List<Field> parameters) {
        String fieldName;
        DataType fieldType;
        Map<String, Object> mapTemp = new HashMap<String, Object>();
        for (int i = 0; i < parameters.size(); i++) {
            Field parameter = parameters.get(i);
            fieldName = parameter.getName();
            fieldType = parameter.getDataType();
            mapTemp.put(fieldName, assignValue(service, fieldType));
        }
        return mapTemp;
    }

    private static Object assignValue(Service service, DataType fieldType) {
        Object randomValue = null;
        switch (fieldType.getKind()) {
            case VOID:
                break;
            case BOOLEAN:
                randomValue = Math.round(Math.random()) == 1 ? "true" : "false";
                break;
            case BYTE:
                randomValue = (byte) (Math.random() * 256 - 128);
                break;
            case SHORT:
                randomValue = Math.round(Math.random() * 100);
                break;
            case INTEGER:
                randomValue = Math.round(Math.random() * 1000);
                break;
            case LONG:
                randomValue = Math.round(Math.random() * 1000);
                break;
            case DOUBLE:
                randomValue = Math.random() * 100;
                break;
            case STRING:
                randomValue = "sampleDataString";
                break;
            case BINARY:
                randomValue = "546869732049732041205465737420427974652041727261792E";
                break;
            case MAP:
                DataType keyType = fieldType.getKeyType();
                DataType valueType = fieldType.getValueType();
                Map<Object, Object> mapTemp = new HashMap<Object, Object>();
                Object key = assignValue(service, keyType);
                Object value = assignValue(service, valueType);
                mapTemp.put(key, value);

                randomValue = mapTemp;
                break;
            case LIST:
                List list = new ArrayList<Object>();
                DataType listValueType = fieldType.getValueType();
                list.add(assignValue(service, listValueType));
                list.add(assignValue(service, listValueType));

                randomValue = list;
                break;
            case SET:
                Set set = new HashSet<Object>();
                DataType setValueType = fieldType.getValueType();
                set.add(assignValue(service, setValueType));
                set.add(assignValue(service, setValueType));
                randomValue = set;
                break;
            case ENUM:
                List<TEnum> structsE = service.getEnumDefinitions();
                for (int i = 0; i < structsE.size(); i++) {
                    TEnum tenum = structsE.get(i);
                    if ((tenum.getNamespace() + "." + tenum.getName()) == fieldType.qualifiedName) {
                        int size = tenum.enumItems.size();
                        int index = (int) (Math.random() * size);
                        return tenum.enumItems.get(index).label;
                    }
                }
                return "";
            case STRUCT:
                List<Struct> structs = service.getStructDefinitions();
                for (int i = 0; i < structs.size(); i++) {
                    Struct struct = structs.get(i);
                    if ((struct.getNamespace() + '.' + struct.getName()).equals(fieldType.getQualifiedName())) {
                        randomValue = getSample(service, struct.getFields());
                    }
                }
                break;

            case DATE:
                randomValue = "2016/06/16 16:00";
                break;
            case BIGDECIMAL:
                randomValue = "1234567.123456789123456";
                break;
            default:
                randomValue = "";
        }
        return randomValue;
    }

    public static String readFromeFile(String jsonFile) {
        StringBuilder sb = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(Paths.get(jsonFile), StandardCharsets.UTF_8);
            for (String line : lines) {
                sb.append(line).append(System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String readFromeFileByline(String jsonFile) {
        StringBuilder sb = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(Paths.get(jsonFile), StandardCharsets.UTF_8);
            for (String line : lines) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static List<String> readFromeFile2List(String jsonFile) {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(jsonFile), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static String post(String service,
                              String version,
                              String method,
                              String parameter) {

        InvocationContextImpl invocationCtx = (InvocationContextImpl) SetCmd.invocationContext;
        invocationCtx.serviceName(service);
        invocationCtx.versionName(version);
        invocationCtx.methodName(method);
        invocationCtx.callerMid("CmdCaller");

        logger.info("inCtx info: {}", invocationCtx.toString());
        if (!invocationCtx.timeout().isPresent()) {
            //设置请求超时时间,从环境变量获取，默认 10s ,即 10000
            Integer timeOut = Integer.valueOf(getEnvTimeOut());
            invocationCtx.timeout(timeOut);
        }
        invocationCtx.codecProtocol(CodecProtocol.CompressedBinary);
        Service bizService = ServiceCache.getService(service, version);
        if (bizService == null) {
            System.out.println("bizService not found[service:" + service + ", version:" + version + "]");
            return String.format("{\"" +
                    "\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", SoaCode.NoMatchedService.getCode(), SoaCode.NoMatchedService.getMsg(), "{}");
        }

        logger.info("the current invocationContext Info : {}", invocationCtx.toString());

        //fillInvocationCtx(invocationCtx, req);
        //set invocationCtx to threadLocal
        InvocationContextImpl.Factory.currentInstance(invocationCtx);
        JsonPost jsonPost = new JsonPost(service, version, method, true);
        try {
            return jsonPost.callServiceMethod(parameter, new OptimizedMetadata.OptimizedService(bizService));
        } catch (SoaException e) {

            System.out.println(e.getMsg());
            return String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", e.getCode(), e.getMsg(), "{}");

        } catch (Exception e) {

            System.out.println(e.getMessage());
            return String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", "9999", "系统繁忙，请稍后再试[9999]！", "{}");
        } finally {
            InvocationContextImpl.Factory.removeCurrentInstance();
        }
    }

    private static String getEnvTimeOut() {
        String timeOut = System.getenv(OPEN_API_TIMEOUT.replaceAll("\\.", "_"));
        if (timeOut == null) {
            timeOut = System.getProperty(OPEN_API_TIMEOUT);
        }
        if (timeOut == null) {
            timeOut = "10000";
        }
        return timeOut;
    }
}
