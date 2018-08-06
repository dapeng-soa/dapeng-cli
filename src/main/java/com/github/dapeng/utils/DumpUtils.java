package com.github.dapeng.utils;

import com.github.dapeng.core.helper.IPUtils;
import sun.misc.Unsafe;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author hui
 * @date 2018/8/3 0003 11:39
 */
public class DumpUtils {


    public static class NodeCount {
        private String serviceName;
        private String ruleTypeName;
        private int key;
        private int timestamp;
        private int minCount;
        private int midCount;
        private int maxCount;

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getRuleTypeName() {
            return ruleTypeName;
        }

        public int getKey() {
            return key;
        }

        public int getTimestamp() {
            return timestamp;
        }

        public int getMinCount() {
            return minCount;
        }

        public int getMidCount() {
            return midCount;
        }

        public int getMaxCount() {
            return maxCount;
        }

    }

    /**
     * nodePage数量, 128K
     */
    private final static int NODE_PAGE_COUNT = 128 * 1024;

    /**
     * dictionaryItem数量，2K
     */
    private final static int DICTION_ITEM_COUNT = 2048;
    /**
     * Root Page:存储基本的信息,该域占用4K
     */
    private final static long DICTION_ROOT_OFFSET = 4096;
    /**
     * DictionRoot域的地址偏移量, 该域占用12K
     */
    private final static long DICTION_DATA_OFFSET = DICTION_ROOT_OFFSET + 12 * 1024;
    /**
     * DictionData域的地址偏移量, 该域占用128K
     */
    private final static long NODE_PAGE_OFFSET = DICTION_DATA_OFFSET + 128 * 1024;
    /**
     * 整块共享内存的大小(4K+12K+128K+128M)
     */
    private final static int TOTAL_MEM_BYTES = 134365184;

    /**
     * 基准时间 2018-08-01 00:00:00
     */
    private final static long BASE_TIME_MILLIS = 1533052800000L;
    /**
     * 内存操作对象
     */
    private static Unsafe unsafe;
    /**
     * 共享内存起始地址
     */
    private static long homeAddr;

    /**
     * 持有buffer的强引用, 以防止该对象给gc回收
     */
    private static MappedByteBuffer buffer;


    /**
     * 获取指定共享内存文件的限流数据
     *
     * @param inputFile 指定的共享内存文件
     */
    public static void getFreqData(String inputFile) throws NoSuchFieldException, IllegalAccessException, IOException {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        unsafe = (Unsafe) f.get(null);
        //File file = new File(System.getProperty("user.home")+"/shm.data");
        File file = new File(inputFile);
        RandomAccessFile access = new RandomAccessFile(file, "r");
        buffer = access.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, TOTAL_MEM_BYTES);
        Field address = Buffer.class.getDeclaredField("address");
        address.setAccessible(true);
        homeAddr = (Long) address.get(buffer);

        List<NodeCount> freqData = new ArrayList<>(16);

        for (int pageIndex = 0; pageIndex < NODE_PAGE_COUNT; pageIndex++) {
            long nodePageAddr = homeAddr + NODE_PAGE_OFFSET + 1024 * pageIndex;
            short nodes = unsafe.getShort(nodePageAddr + Integer.BYTES * 2);
            if (nodes != 0) {
                long nodeAddr = nodePageAddr + 16;
                for (int nodeIndex = 0; nodeIndex < nodes; nodeIndex++) {
                    NodeCount temp = new NodeCount();
                    short appId = unsafe.getShort(nodeAddr + nodeIndex * 24);
                    short ruleTypeId = unsafe.getShort(nodeAddr + nodeIndex * 24 + Short.BYTES);
                    temp.key = unsafe.getInt(nodeAddr + nodeIndex * 24 + Short.BYTES * 2);
                    temp.timestamp = unsafe.getInt(nodeAddr + nodeIndex * 24 + Short.BYTES * 2 + Integer.BYTES);
                    temp.minCount = unsafe.getInt(nodeAddr + nodeIndex * 24 + Short.BYTES * 2 + Integer.BYTES * 2);
                    temp.midCount = unsafe.getInt(nodeAddr + nodeIndex * 24 + Short.BYTES * 2 + Integer.BYTES * 3);
                    temp.maxCount = unsafe.getInt(nodeAddr + nodeIndex * 24 + Short.BYTES * 2 + Integer.BYTES * 4);
                    temp.serviceName = findName(appId);
                    temp.ruleTypeName = findName(ruleTypeId);
                    freqData.add(temp);
                }
            }
        }
        freqData.sort(Comparator.comparing(NodeCount::getServiceName));

        for (NodeCount node : freqData) {
            if(node.getRuleTypeName().contains("Ip")) {
                System.out.println(" serviceName:" + node.getServiceName() +
                        "  ruleType:" + node.getRuleTypeName() +
                        "  key:" + transferIp(node.getKey()) +
                        "  timestamp:" + transferTimestamp(node.getTimestamp()) +
                        "  [min_count/mid_count/max_count]:" + node.getMinCount() + "/" + node.getMidCount() + "/" + node.getMaxCount());
            }else {
                System.out.println(" serviceName:" + node.getServiceName() +
                        "  ruleType:" + node.getRuleTypeName() +
                        "  key:" + node.getKey() +
                        "  timestamp:" + transferTimestamp(node.getTimestamp()) +
                        "  [min_count/mid_count/max_count]:" + node.getMinCount() + "/" + node.getMidCount() + "/" + node.getMaxCount());
            }
        }
    }

    /**
     * 通过appId、ruleTypeId 查找对应的serviceName、ruleTypeName
     *
     * @param id appId 、ruleTypeId
     * @return serviceName、ruleTypeName
     */
    private static String findName(short id) {
        for (int itemIndex = 0; itemIndex < DICTION_ITEM_COUNT; itemIndex++) {
            short typeIdTemp = unsafe.getShort(homeAddr + DICTION_ROOT_OFFSET + itemIndex * 6 + Short.BYTES);
            if (id == typeIdTemp) {
                short typeLength = unsafe.getShort(homeAddr + DICTION_ROOT_OFFSET + itemIndex * 6);
                short typeOffset = unsafe.getShort(homeAddr + DICTION_ROOT_OFFSET + itemIndex * 6 + Short.BYTES * 2);
                long dataAddr = homeAddr + DICTION_DATA_OFFSET + typeOffset * 2;
                char[] type = new char[typeLength];
                for (int i = 0; i < typeLength; i++) {
                    type[i] = (char) unsafe.getByte(dataAddr + i);
                }
                return String.valueOf(type);
            }
        }
        return "";
    }

     /**
      * IP转换，int---->String
      */
    private static String transferIp(int ip) {
        return (ip >>> 24) + "."
                + (ip << 8 >>> 24) + "."
                + (ip << 16 >>> 24) + "."
                + (ip & 0x000000ff);
    }

     /**
      * 时间戳转换， int ----> String
      */
    private static String transferTimestamp(int timestamp){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(timestamp * 1000 + BASE_TIME_MILLIS);
        return formatter.format(date);
    }
}
