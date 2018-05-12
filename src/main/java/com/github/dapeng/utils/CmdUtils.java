package com.github.dapeng.utils;

import com.github.dapeng.core.metadata.Service;
import com.github.dapeng.openapi.cache.ServiceCache;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CmdUtils {
    private static final Logger logger = LoggerFactory.getLogger(CmdUtils.class);

    public static void writeMsg(Context context, String content) {
        IOConsole ioc = context.getIoConsole();
        ioc.println(content);
    }

    public static void writeFormatMsg(Context context, String format, Object... var2) {
        IOConsole ioc = context.getIoConsole();
        String info = String.format(format, var2);
        ioc.println(info);
    }


    public static Map<String, String> getCmdArgs(Context context) {
        Map<String, String> args = new HashMap<>();
        String[] inputArgs = (String[]) context.getValue(Context.KEY_COMMAND_LINE_ARGS);

        if (inputArgs != null && inputArgs.length > 0) {
            for (int i = 0; i < inputArgs.length; i++) {
                String arg = inputArgs[i];
                switch (arg) {
                   /* case CmdProperties.KEY_ARGS_PATH:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_PATH, inputArgs[i+1]);
                        }
                        break;*//*
                    case CmdProperties.KEY_ARGS_DATA:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_DATA, inputArgs[i + 1]);
                        }
                        break;
                    case CmdProperties.KEY_ARGS_ROUTE:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_ROUTE, inputArgs[i + 1]);
                        }
                        break;
                    case CmdProperties.KEY_ARGS_SERVICE:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_SERVICE, inputArgs[i + 1]);
                        }
                        break;
                    case CmdProperties.KEY_ARGS_RUNTIME:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_RUNTIME, CmdProperties.RUNTIME_PATH + "/" + inputArgs[i + 1]);
                        } else {
                            args.put(CmdProperties.KEY_ARGS_RUNTIME, CmdProperties.RUNTIME_PATH);
                        }
                        break;
                    case CmdProperties.KEY_ARGS_CONFIG:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_CONFIG, CmdProperties.CONFIG_PATH + "/" + inputArgs[i + 1]);
                        } else {
                            args.put(CmdProperties.KEY_ARGS_CONFIG, CmdProperties.CONFIG_PATH);
                        }
                        break;
                    case CmdProperties.KEY_ARGS_LIST:
                        args.put(CmdProperties.KEY_ARGS_LIST, CmdProperties.KEY_ARGS_LIST);
                        break;
                    case CmdProperties.KEY_ARGS_SERVICE_METHOD:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_SERVICE_METHOD, inputArgs[i + 1]);
                        }
                        break;
                    case CmdProperties.KEY_ARGS_FILE_READ:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_FILE_READ, inputArgs[i + 1]);
                        }
                        break;

                    case CmdProperties.KEY_ARGS_FILE_OUT:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_FILE_READ, inputArgs[i + 1]);
                        }
                        break;
                    case CmdProperties.KEY_ARGS_VERSION:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_VERSION, inputArgs[i + 1]);
                        }
                        break;*/
                    /*case CmdProperties.KEY_ARGS_SERVICE_CALL:
                        if ((i + 4) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_SERVICE_CALL, inputArgs[i + 1] + ":" + inputArgs[i + 2] + ":" + inputArgs[i + 3] + ":" + inputArgs[4]);
                        }
                        break;*/
                    /*case CmdProperties.KEY_ARGS_PASSWD:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_PASSWD, inputArgs[i + 1]);
                        }
                        break;
                   *//* case CmdProperties.KEY_ARGS_HOST:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_HOST, inputArgs[i+1]);
                        }
                        break;*//*
                    case CmdProperties.KEY_ARGS_USER:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_USER, inputArgs[i + 1]);
                        }
                        break;*/

                   /* case CmdProperties.KEY_ARGS_METADATA:
                        if ((i + 2) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_METADATA, inputArgs[i + 1] + "#" + inputArgs[i + 2]);
                        }
                        break;*/
                    default:
                        if ((i + 1) < inputArgs.length && arg.contains("-")) {
                            args.put(arg, inputArgs[i + 1]);
                        }else if((i + 1) == inputArgs.length && arg.contains("-")){
                            args.put(arg, arg);
                        }
                        break;
                }
            }
        }
        return args;
    }

    public static boolean isEmpty(String value) {
        return value == null || value.length() <= 0;
    }

    public static Map<String, String> _getCmdArgs(Context ctx) {
        String cmdLine = (String) ctx.getValue(Context.KEY_COMMAND_LINE_INPUT);
        cmdLine+=" ";//不加" "  类似这种会报错 service -list
        logger.info("[_getCmdArgs] ==>cmdLine=[{}]",cmdLine);
        Map<String, String> args = new HashMap<>();
        int charIndex = 0;

        while (charIndex < cmdLine.length()) {
            int startIndex = cmdLine.indexOf('-', charIndex);
            int endIndex = cmdLine.indexOf(' ', startIndex);
            String key = cmdLine.substring(startIndex, endIndex);

            startIndex = cmdLine.indexOf(' ', endIndex);
            endIndex = cmdLine.indexOf('-', startIndex) > 0 ? cmdLine.indexOf('-', startIndex) : cmdLine.length();
            String value = cmdLine.substring(startIndex, endIndex);
            charIndex = endIndex;
            if(value.trim().isEmpty()){
                args.put(key.trim(), key.trim());
            }else{
                args.put(key.trim(), value.trim());
            }
        }
        return args;
    }

    public static String getResult(Object obj) {
        String empty_data = "the data is empty...";
        if (obj == null) return empty_data;

        if (obj instanceof List) {
            List<String> stringList = (List<String>) obj;
            if (stringList != null && !stringList.isEmpty()) {
                StringBuilder info = new StringBuilder();
                stringList.forEach(item -> {
                    info.append(item);
                    info.append("\r\n");
                });
                return info.toString();
            } else {
                return empty_data;
            }
        }

        if (obj instanceof String) {
            if (!((String) obj).isEmpty()) {
                return (String) obj;
            } else {
                return empty_data;
            }
        }
        return empty_data;
    }

    public static List<String> getRuntimeServiceMethods(Context context, String serviceName) {
        if (!ZookeeperUtils.isContextInitialized()) {
            ZookeeperUtils.connect();
        }
        logger.info("[getRuntimeServiceMethods] ==>ServiceCache.getServices()=[{}]", ServiceCache.getServices());
        List<Map.Entry<String, Service>> serviceMapLists = ServiceCache.getServices().entrySet().stream().filter(i -> (i.getValue().getNamespace() + "." + i.getKey().split(":")[0]).equalsIgnoreCase(serviceName)).collect(Collectors.toList());
        if (serviceMapLists != null && !serviceMapLists.isEmpty()) {
            List<String> methods = new ArrayList<>();
            serviceMapLists.get(0).getValue().getMethods().stream().forEach(i -> {
                methods.add(i.getName());
            });
            logger.info("[getRuntimeServiceMethods] ==>methods=[{}]", methods);
            //Collections.sort(methods);
            methods.sort(String::compareToIgnoreCase);
            return methods;
        } else {
            CmdUtils.writeMsg(context, " the service[" + serviceName + "] not found... ");
            return null;
        }
    }

    public static void handledStatus(Context context, boolean handled,String cmdUsage) {
        //没有处理  打印help info
        if (!handled) {
            CmdUtils.writeMsg(context, cmdUsage);
        }
    }

}
