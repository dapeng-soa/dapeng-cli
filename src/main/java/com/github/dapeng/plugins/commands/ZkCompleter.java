package com.github.dapeng.plugins.commands;

import com.github.dapeng.utils.ZookeeperUtils;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import jline.internal.Log;
import org.clamshellcli.api.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ZkCompleter implements Completer {
    private static final Logger logger = LoggerFactory.getLogger(ZkController.class);
    StringsCompleter cmdNamesCompleter;
    Map<String,List<String>> cmdHints;

    public ZkCompleter(List<Command> cmds){
        cmdHints = extractCommandHints(cmds);
    }

    @Override
    public int complete(String input, int cursor, List<CharSequence> result) {
        String cmd = input != null ? input.substring(0, cursor) : "";
        
        logger.info("ZkCompleter.complete ==>input = {}",input);
        // display all avail cmds
        if (cmd.isEmpty() && cmdHints != null) {
            for (Map.Entry<String,List<String>> e : cmdHints.entrySet()) {
                result.add(e.getKey());
            }
        }

        // look for commands that match input
        if (!cmd.isEmpty() && cmdHints != null) {
            for (Map.Entry<String,List<String>> e : cmdHints.entrySet()){

                //Log.info("e.getKey: " + e.getKey() + " input: " + input.trim() + " match: " + e.getKey().startsWith(input.trim()));
                // exact match: display arcuments, exit.
                if (e.getKey().equals(input.trim())){
                    result.addAll(e.getValue());
                    break;
                } else if (e.getKey().startsWith(input.trim())){
                    result.add(e.getKey());
                }

                // display all partial match
                String[] inputCandidates = input.split("\\s+");

//                if (inputCandidates.length == 1) {
//                    if (e.getKey().startsWith(inputCandidates[0])){
//                        result.add(e.getKey());
//                    }
//                }

                //handle args candidates like:  zk -g, will complete the input to zk -get
                if (inputCandidates.length > 1) {
                    if (e.getKey().equals(inputCandidates[0])) {
                        for (String cmdArg: e.getValue()) {
                            if (cmdArg.equals(inputCandidates[inputCandidates.length - 1])) {
                                break;
                            } else if (cmdArg.startsWith(inputCandidates[inputCandidates.length - 1])) {
                                result.add(cmdArg);
                            }
                        }
                    }
                }

                if (inputCandidates.length > 2) {
                    if (e.getKey().equals(inputCandidates[0])) {
                        String lastInputCandidate = inputCandidates[inputCandidates.length - 1];
                        List<String> childPaths = ZookeeperUtils.getChildren(lastInputCandidate);
                        if (childPaths != null && childPaths.size() > 0) {
                            for (String childPath: childPaths) {
                                if (lastInputCandidate.equals( "/")) {
                                    result.add(lastInputCandidate + childPath);
                                } else {
                                    result.add(lastInputCandidate + "/" + childPath);
                                }
                            }
                        } else {
                            try {
                                //尝试补全机制 /so => /soa
                                if (lastInputCandidate.charAt(0) == '/') {
                                    String parentZkPath = lastInputCandidate.lastIndexOf("/") == 0 ? "/" : lastInputCandidate.substring(0, lastInputCandidate.lastIndexOf("/"));
                                    childPaths = ZookeeperUtils.getChildren(parentZkPath);
                                    if (childPaths != null && childPaths.size() > 0) {
                                        String lastNodePath = lastInputCandidate.substring(lastInputCandidate.lastIndexOf("/") + 1);

                                        for (String childPath: childPaths) {
                                            if (childPath.contains(lastNodePath)) {
                                                if (parentZkPath .equals("/")) {
                                                    result.add(parentZkPath + childPath);
                                                } else {
                                                    result.add(parentZkPath + "/" + childPath);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                   //模糊匹配模式
                                   Set<String> allZkPath = ZookeeperUtils.getAllZkPaths();
                                   for (String p: allZkPath) {
                                       if (p.substring(p.lastIndexOf("/")).contains(lastInputCandidate)) {
                                           result.add(p);
                                       }

                                   }

                                }
                            } catch (Exception ex) {
                                Log.trace(ex.getCause() + ex.getMessage());
                            }
                        }
                    }
                }
            }
        }

       /* result.forEach(item -> logger.info("remove before keys :{}",item));
        //去掉 runtime目录
        //List<CharSequence> ignoreKeys =  result.stream().filter(x -> x.toString().equalsIgnoreCase("/soa/runtime")).collect(Collectors.toList());

        logger.info("remove soa/runtime :");
        List<CharSequence> ignoreKeys =  result.stream().filter(x -> x.toString().contains("soa/runtime")).collect(Collectors.toList());
        ignoreKeys.forEach(item -> logger.info("ignore[ soa/runtime]  keys :{}",item));
        result.removeAll(ignoreKeys);

        result.forEach(item -> logger.info("remove after keys :{}",item));*/


        logger.info("[complete] ==> remove before result:[{}]", result);

       List<String> filterKeys = new ArrayList<String>();
        filterKeys.add("soa/runtime");//runtime 路径不能随便修改

        List<CharSequence> ignoreKeys = result.stream().filter(res -> filterKeys.stream().filter(filter -> res.toString().contains(filter)).toArray().length > 0).collect(Collectors.toList());
        logger.info("[complete] ==> filterKeys :{}----ignoreKeys:{}", filterKeys, ignoreKeys);

        result.removeAll(ignoreKeys);
        logger.info("[complete] ==> remove after result:[{}]", result);

        return result.isEmpty() ? -1 : cmd.length() + 1;
    }

    /**
     * Returns a map of [cmd-name][cmd args[]]
     * @param commands
     * @return
     */
    private static Map<String,List<String>> extractCommandHints (List<Command> commands) {
        Map<String, List<String>> result = new TreeMap<String,List<String>>();

        for (Command cmd : commands) {
            if (cmd.getDescriptor() != null){
                String cmdStr = cmd.getDescriptor().getName();
                result.put(cmdStr, extractArgs(cmd));
            }
        }
        return result;
    }

    private static List<String> extractArgs(Command cmd) {
        if (cmd.getDescriptor() == null || cmd.getDescriptor().getArguments() == null) {
            return null;
        }
        List<String> args = new ArrayList<String>(cmd.getDescriptor().getArguments().size());
        for (Map.Entry<String,String> e : cmd.getDescriptor().getArguments().entrySet()){
            args.add(e.getKey());
        }

        return args;
    }


}
