package com.github.dapeng.utils;

import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;

import java.util.HashMap;
import java.util.Map;

public class CmdUtils {

    public static void writeMsg(Context context, String content) {
        IOConsole ioc = context.getIoConsole();
        ioc.println(content);
    }

    public static void writeFormatMsg(Context context,String format,Object... var2) {
        IOConsole ioc = context.getIoConsole();
        String info = String.format(format,var2);
        ioc.println(info);
    }


    public static Map<String, String> getCmdArgs(Context context) {
        Map<String, String> args = new HashMap<>();
        String[] inputArgs = (String[])context.getValue(Context.KEY_COMMAND_LINE_ARGS);

        if (inputArgs != null && inputArgs.length > 0) {
            for (int i = 0; i < inputArgs.length; i++) {
                String arg = inputArgs[i];
                switch (arg) {
                    case CmdProperties.KEY_ARGS_PATH:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_PATH, inputArgs[i+1]);
                        }
                        break;
                    case CmdProperties.KEY_ARGS_DATA:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_DATA, inputArgs[i+1]);
                        }
                        break;
                    case CmdProperties.KEY_ARGS_SERVICE:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_SERVICE, inputArgs[i+1]);
                        }
                        break;
                    case CmdProperties.KEY_ARGS_RUNTIME:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_RUNTIME, CmdProperties.RUNTIME_PATH + "/" + inputArgs[i+1]);
                        } else {
                            args.put(CmdProperties.KEY_ARGS_RUNTIME, CmdProperties.RUNTIME_PATH);
                        }
                        break;
                    case CmdProperties.KEY_ARGS_CONFIG:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_CONFIG, CmdProperties.CONFIG_PATH + "/" + inputArgs[i+1]);
                        } else {
                            args.put(CmdProperties.KEY_ARGS_CONFIG, CmdProperties.CONFIG_PATH);
                        }
                        break;
                    case CmdProperties.KEY_ARGS_SERVICE_LIST:
                        args.put(CmdProperties.KEY_ARGS_SERVICE_LIST, CmdProperties.KEY_ARGS_SERVICE_LIST);
                        break;
                    case CmdProperties.KEY_ARGS_SERVICE_METHOD:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_SERVICE_METHOD, inputArgs[i+1]);
                        }
                        break;
                    case CmdProperties.KEY_ARGS_FILE:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_FILE, inputArgs[i+1]);
                        }
                        break;
                    case CmdProperties.KEY_ARGS_VERSION:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_VERSION, inputArgs[i+1]);
                        }
                        break;
                    case CmdProperties.KEY_ARGS_SERVICE_CALL:
                        if ((i + 4) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_SERVICE_CALL, inputArgs[i+1] + ":" + inputArgs[i+2] + ":" + inputArgs[i+3] + ":" + inputArgs[4]);
                        }
                        break;
                    case CmdProperties.KEY_ARGS_PASSWD:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_PASSWD, inputArgs[i+1]);
                        }
                        break;
                    case CmdProperties.KEY_ARGS_HOST:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_HOST, inputArgs[i+1]);
                        }
                        break;
                    case CmdProperties.KEY_ARGS_USER:
                        if ((i + 1) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_USER, inputArgs[i+1]);
                        }
                        break;

                    case CmdProperties.KEY_ARGS_METADATA:
                        if ((i + 2) <= inputArgs.length) {
                            args.put(CmdProperties.KEY_ARGS_METADATA, inputArgs[i+1]+"#"+inputArgs[i+2]);
                        }
                        break;
                    //default: break;
                    default:
                        if ((i + 1) <= inputArgs.length && arg.contains("-")) {
                            args.put(arg, inputArgs[i+1]);
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
        String  cmdLine = (String) ctx.getValue(Context.KEY_COMMAND_LINE_INPUT);
        Map<String, String> args = new HashMap<>();
        int charIndex = 0;

        while (charIndex < cmdLine.length()) {
            int startIndex = cmdLine.indexOf('-', charIndex);
            int endIndex = cmdLine.indexOf(' ', startIndex);
            String key = cmdLine.substring(startIndex, endIndex);

            startIndex = cmdLine.indexOf(' ', endIndex);
            endIndex = cmdLine.indexOf('-', startIndex) > 0 ? cmdLine.indexOf('-', startIndex) : cmdLine.length();
            String value = cmdLine.substring(startIndex,endIndex);
            charIndex = endIndex;
            args.put(key.trim(), value.trim());
        }
        return args;
    }
}
