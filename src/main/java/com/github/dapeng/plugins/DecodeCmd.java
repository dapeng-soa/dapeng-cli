package com.github.dapeng.plugins;

import com.github.dapeng.utils.*;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.clamshellcli.api.Command;
import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * json 反序列化
 */
public class DecodeCmd implements Command {
    private static final Logger logger = LoggerFactory.getLogger(LogAnalysCmd.class);

    private static final String NAMESPACE = "dapeng";
    private static final String ACTION_NAME = "decode";

    @Override
    public Command.Descriptor getDescriptor() {
        return new Command.Descriptor() {
            public String getNamespace() {
                return NAMESPACE;
            }

            public String getName() {
                return ACTION_NAME;
            }

            public String getDescription() {
                return "decode the req/resp Binary to json. ";
            }

            public String getUsage() {
                StringBuilder sb = new StringBuilder();
                sb.append(Configurator.VALUE_LINE_SEP).append(" decode -s");
                sb.append(Configurator.VALUE_LINE_SEP).append(" decode -v");
                sb.append(Configurator.VALUE_LINE_SEP).append(" decode -m");
                sb.append(Configurator.VALUE_LINE_SEP).append(" decode -req");
                sb.append(Configurator.VALUE_LINE_SEP).append(" decode -resp");
                sb.append(Configurator.VALUE_LINE_SEP).append(" decode -f");
                sb.append(Configurator.VALUE_LINE_SEP).append(" decode -o");
                return sb.toString();
            }

            Map<String, String> args = null;

            public Map<String, String> getArguments() {
                if (args != null) return args;
                args = new LinkedHashMap();
                args.put(CmdProperties.KEY_ARGS_SERVICE, "type '-s service' to specific service(package + serviceName).");
                args.put(CmdProperties.KEY_ARGS_VERSION, "type '-v serviceVersion' to specific serviceVersion.");
                args.put(CmdProperties.KEY_ARGS_SERVICE_METHOD, "type '-m method' to specific service method.");
                args.put(CmdProperties.KEY_ARGS_FILE_READ, "type '-f file(path + fileName)' to get request json content from file.");
                args.put(CmdProperties.KEY_ARGS_FILE_OUT, "type '-o file(path + fileName)' to save data to file.");
                args.put(CmdProperties.KEY_ARGS_REQ, "type '-req' to Deserialize request.");
                args.put(CmdProperties.KEY_ARGS_RESP, "type '-resp'  to Deserialize response.");
                return args;
            }
        };
    }


    /**
     * 1. 获取运行时服务的 方法列表   log  -date
     *
     * @param context
     * @return
     */
    @Override
    public Object execute(Context context) {
        boolean handled = false;
        Map<String, String> inputArgs = CmdUtils._getCmdArgs(context);
        logger.info("[execute] ==> inputArgs=[{}]", inputArgs);

        String args_service = inputArgs.get(CmdProperties.KEY_ARGS_SERVICE);
        String args_method = inputArgs.get(CmdProperties.KEY_ARGS_SERVICE_METHOD);
        String args_version = inputArgs.get(CmdProperties.KEY_ARGS_VERSION);

        String args_req = inputArgs.get(CmdProperties.KEY_ARGS_REQ);
        String args_resp = inputArgs.get(CmdProperties.KEY_ARGS_RESP);

        String file_out = inputArgs.get(CmdProperties.KEY_ARGS_FILE_OUT);
        String file_read = inputArgs.get(CmdProperties.KEY_ARGS_FILE_READ);

        if (CmdUtils.isNotEmpty(args_service) && CmdUtils.isNotEmpty(args_method) && CmdUtils.isNotEmpty(args_version) && CmdUtils.isNotEmpty(file_read)) {
            String hexStr = ServiceUtils.readFromeFileByline(file_read);

            String parseType = null;
            if (CmdUtils.isNotEmpty(args_req)) parseType = "request";
            if (CmdUtils.isNotEmpty(args_resp)) parseType = "response";

            if (CmdUtils.isNotEmpty(parseType)) {
                String result = JsonSerializerUtils.toJson(args_service, args_method, args_version, hexStr, parseType);
                //-o
                if (CmdUtils.isNotEmpty(file_out)) {
                    ServiceUtils.writerFile(context, file_out, result);
                } else {
                    //print console
                    CmdUtils.writeMsg(context, result);
                }
                handled = true;
            }
        }
        CmdUtils.handledStatus(context, handled, this.getDescriptor().getUsage());
        return null;
    }

    @Override
    public void plug(Context context) {
    }

    @Override
    public void unplug(Context context) {
    }


    private static String loadErrorLog(String date, List listParamMap) {
        String url = "http://es.today36524.td/dapeng_log_index-" + date + "/_search";

        String _response = HttpUtils.doPostJson(url, buildQueryMap(listParamMap, 1), null).replaceAll("/", "");
        int total = getTotal(_response);

        if (total <= 0) return "load empty data.";
        String response = HttpUtils.doPostJson(url, buildQueryMap(listParamMap, total), null).replaceAll("/", "");
        Map respmap = new Gson().fromJson(response, Map.class);
        List<Map> itemList = (List<Map>) ((Map) respmap.get("hits")).get("hits");
        StringBuilder sb = new StringBuilder();
        sb.append("------------------------------------ 共计【" + total + "】条记录 ------------------------------------").append("\n\n");
        for (Map _map : itemList) {
            Map item = (Map) _map.get("_source");
            sb.append("---------------------------------------------------------------------------------------------").append("\n");
            sb.append("***  " + String.format("%-40s", "   logtime:[" + item.get("logtime")) + "]  **  " + String.format("%-35s", "    hostname:[" + item.get("hostname")) + "]  ***\n");
            sb.append("***  " + String.format("%-40s", "sessionTid:[" + item.get("sessionTid")) + "]  **  " + String.format("%-35s", "         tag:[" + item.get("tag")) + "]  ***\n");
            sb.append("---------------------------------------------------------------------------------------------").append("\n");
            sb.append(item.get("message")).append("\n");
            sb.append("*******************************************************").append("\n\n");
        }
        return sb.toString();
    }

    private static int getTotal(String response) {
        if (StringUtils.isBlank(response)) return 0;
        Map respmap = new Gson().fromJson(response, Map.class);
        return ((Double) ((Map) respmap.get("hits")).get("total")).intValue();
        /*return String.valueOf(((Double) ((Map) respmap.get("hits")).get("total")).intValue());*/
    }

    private static Map buildQueryMap(List<Map> listParam, int size) {
        //Map mustMap = new HashMap();
        Map filterMap = putMap(null, "script", putMap(null, "script", putMap(null, "source", "doc['message'].values.length==0")));
        Map boolMap = new HashMap();
        putMap(boolMap, "must", listParam);
        putMap(boolMap, "filter", filterMap);

        Map root = new HashMap();
        putMap(root, "query", putMap(null, "bool", boolMap));
        putMap(root, "from", 0);
        putMap(root, "size", size);

        return root;
    }

    public static Map putMap(Map map, String key, Object value) {
        if (map == null) {
            map = new HashMap();
        }
        map.put(key, value);
        return map;
    }
}