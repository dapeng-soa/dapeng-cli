package com.github.dapeng.test;

import com.github.dapeng.utils.ZookeeperUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ZkTest {

    public static List<String> result;

    public static void main(String[] args) {

        String respondsToRegEx = "(^[help]|^[sysinfo]|^[exit]|zk|service|metadata|request|set|unset)\\b(.*)";
        Pattern pattern = Pattern.compile(respondsToRegEx);


        String cmdLine = "zk -get";
        System.out.println(pattern.matcher(cmdLine).matches());

        cmdLine = "zk";
        System.out.println(pattern.matcher(cmdLine).matches());

        cmdLine = "zk1";
        System.out.println(pattern.matcher(cmdLine).matches());

        cmdLine = "z";
        System.out.println(pattern.matcher(cmdLine).matches());

        cmdLine = "hel";
        System.out.println(pattern.matcher(cmdLine).matches());

        cmdLine = "help";
        System.out.println(pattern.matcher(cmdLine).matches());

       // System.out.println("cmd: " + cmdLine.substring(0, cmdLine.indexOf(" ")) + " equals: " + cmdLine.substring(0, cmdLine.indexOf(" ")).equals("zk"));
        //System.out.println(pattern.matcher(cmdLine.substring(0, cmdLine.indexOf(" "))).matches());

        //ZookeeperUtils.init();

//        String result = ZookeeperUtils.getData("/soa/config/services/test2");
//        System.out.println(" result: " + result );
//        List<String> result = ZookeeperUtils.getChildren("/soa/runtime/services/com.isuwang.soa.settle.service.SettleService");
//        System.out.println(result);


        ZookeeperUtils.connect();

        Set<String> result = getAllNodes("/", new HashSet<String>());

    }


//    private static List<String> fuzzySearch(String path, String content) {
//        List<String> paths = ZookeeperUtils.getChildren(path);
//
//    }

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


//    private def getAllNodes(path: String, result: Set[String]): Set[String] = {
//        val childs = ZookeeperUtils.getChildren(path)
//        if (childs == null || childs.size() <= 0) {
//            result
//        } else {
//            childs.asScala.flatMap(i => {
//                    val p = if (path != "/") s"$path/$i" else s"$path$i"
//            getAllNodes(p, result ++ Set(p))
//        }).toSet
//        }
//    }

}
