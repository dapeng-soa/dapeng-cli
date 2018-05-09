package com.github.dapeng.utils;

import com.github.dapeng.openapi.cache.ServiceCache;
import jline.internal.Log;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static com.github.dapeng.utils.CmdProperties.KEY_SOA_ZOO_KEEPER_HOST;
import static com.github.dapeng.utils.CmdProperties.RUNTIME_PATH;
import static org.apache.zookeeper.ZooKeeper.States.CONNECTED;

public class ZookeeperUtils {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperUtils.class);
    private static ZooKeeper zk;
    private static String zkHost;

    private static final Set<String> zkPaths = new HashSet<>();
    private static final List<String> runtimeServiceCashes = new ArrayList<String>();
    static CountDownLatch semaphore;

    public static Set<String> getAllZkPaths() {
        return zkPaths;
    }

    /**
     * 检查节点是否存在
     */
    public static boolean exists(String path) {
        try {
            Stat exists = zk.exists(path, false);
            if (exists != null) {
                return true;
            }
            return false;
        } catch (Throwable t) {
        }
        return false;
    }


    /**
     * 异步添加持久化节点回调方法
     */
    private static AsyncCallback.StringCallback persistNodeCreateCallback = (rc, path, ctx, name) -> {
        switch (KeeperException.Code.get(rc)) {
            case CONNECTIONLOSS:
                System.out.println("创建节点:" + path + ",连接断开，重新创建");
                break;
            case OK:
                System.out.println("创建节点:{},成功" + path);
                break;
            case NODEEXISTS:
                System.out.println("创建节点:{},已存在" + path);
                break;
            default:
                System.out.println("创建节点:{},失败" + path);
        }
    };

    /**
     * 连接zookeeper
     * <p>
     * 需要加锁
     */
    public static synchronized void connect() {
        try {
            if (zk != null && zk.getState() == CONNECTED) {
                return;
            }

            semaphore = new CountDownLatch(1);
            zk = new ZooKeeper(getZkHost(), 15000, e -> {

                switch (e.getState()) {
                    case Expired:
                        System.out.println("zookeeper Watcher 到zookeeper Server的session过期，重连");
//                        destroy();
                        reset();
                        break;

                    case SyncConnected:
                        System.out.println("Zookeeper Watcher 已连接 zookeeper Server,Zookeeper host: " + getZkHost());
                        getAllNodes("/", zkPaths);
                        cacheRuntimeServiceList();
                        semaphore.countDown();
                        break;

                    case Disconnected:
                        System.out.println("Zookeeper Watcher 连接不上了");
                        //zk断了之后, 会自动重连
                        break;

                    case AuthFailed:
                        System.out.println("Zookeeper connection auth failed ...");
                        destroy();
                        break;

                    default:
                        break;
                }

            });
            semaphore.await();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static synchronized void reset() {
        destroy();
        connect();
    }


    public static synchronized void destroy() {
        try {
            if (zk != null) {
                zk.close();
                zk = null;
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        ServiceCache.getServices().clear();
        runtimeServiceCashes.clear();
        System.out.println("关闭连接，清空service info caches");
    }

    /**
     * 递归节点创建
     */
    public static void createPath(String path, boolean ephemeral) {

        int i = path.lastIndexOf("/");
        if (i > 0) {
            String parentPath = path.substring(0, i);
            //判断父节点是否存在...
            if (!exists(parentPath)) {
                System.out.println(" Create current path: " + parentPath);
                createPath(parentPath, false);
            }
        }
        if (ephemeral) {
            System.out.println("Step into ephemeral...");
            zk.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, persistNodeCreateCallback, "");

            //添加 watch ，监听子节点变化
//            watchInstanceChange(context);
        } else {
            //System.out.println("Step into Persistent..." + path);
            zk.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, persistNodeCreateCallback, "");

        }
    }

    public static void createData(String path, String data) {
        if (zk == null) {
            connect();
        }
        createPath(path, false);
        if (exists(path)) {
            //System.out.println(" start to set data from: " + path);
            zk.setData(path, data.getBytes(), -1, null, data);
        }
    }

    public static String getData(String path) {
        String zkData = "";
        if (zk == null) {
            connect();
        }
        if (exists(path)) {
            try {
                byte[] data = zk.getData(path, null, null);

                zkData = new String(data);

            } catch (Exception e) {
                System.out.println(" Failed to get zookeeper data " + e.getCause() + e.getMessage());
            }

        } else {
            System.out.println(" path not exists...path: " + path);
        }

        return zkData;
    }

    public static List<String> getChildren(String path) {
        if (zk == null) {
            connect();
        }

        List<String> children = new ArrayList<>();

        if (exists(path)) {
            try {
                children = zk.getChildren(path, watchedEvent -> {
                    //Children发生变化，则重新获取最新的services列表
                    if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                        Log.info("节点变更: 重新获取节点信息.......");
                        getAllNodes(path, zkPaths);
                    }
                });
            } catch (Exception e) {
                System.out.println(" Failed to get children....");
            }
        }

        return children;
    }

    public synchronized static void setZkHost(String host) {
        zkHost = host;
        //System.getenv("soa.zookeeper.host");
        System.setProperty(KEY_SOA_ZOO_KEEPER_HOST, host);
        //SoaSystemEnvProperties.SOA_ZOOKEEPER_HOST
        reset();
        ServiceUtils.resetZk();
    }

    public static String getZkHost() {
        String zkSysProperty = System.getProperty(KEY_SOA_ZOO_KEEPER_HOST);
        logger.info("[getZkHost] ==>System.getProperty(KEY_SOA_ZOO_KEEPER_HOST) =[{}]",zkSysProperty);
        if(zkHost == null) {
            zkHost = zkSysProperty;
        }
        return zkHost == null ? "127.0.0.1:2181" : zkHost;
    }

    private static Set<String> getAllNodes(String path, Set<String> result) {
        List<String> childs = ZookeeperUtils.getChildren(path);
        if (childs == null || childs.size() <= 0) {
            return result;
        } else {
            return childs.stream().flatMap(i -> {
                String p = "/".equals(path) ? path + i : path + "/" + i;
                result.add(p);
                return getAllNodes(p, result).stream();
            }).collect(Collectors.toSet());
        }
    }

    /**
     * 获取zookeeper中的services节点的子节点，并设置监听器
     * <p>
     * 取消每次都重制所有服务信息，采用 增量 和 减量 形式
     *
     * @return
     * @author maple.lei
     */
    private static void cacheRuntimeServiceList() {
        runtimeServiceCashes.clear();
        try {
            List<String> children = zk.getChildren(RUNTIME_PATH, watchedEvent -> {
                //Children发生变化，则重新获取最新的services列表
                if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                    logger.info("[cacheRuntimeServiceList] ==> [{}]子节点发生变化，重新获取子节点...\", watchedEvent.getPath()");
                    cacheRuntimeServiceList();
                }
            });
            children.forEach(serviceName -> runtimeServiceCashes.add(serviceName));
        } catch (KeeperException | InterruptedException e) {
            logger.info(e.getMessage(), e);
        }
    }


    public static List<String> getRuntimeServices() {
        try {
            if (zk == null) {
                connect();
            }
            semaphore.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("[getRunTimeServices] ==> runtimeServiceCashes ={}", runtimeServiceCashes);
        return runtimeServiceCashes;
    }

}
