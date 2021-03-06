package com.github.dapeng.plugins;

import com.github.dapeng.utils.CmdProperties;
import com.github.dapeng.utils.CmdUtils;
import com.github.dapeng.utils.HttpUtils;
import com.github.dapeng.utils.ServiceUtils;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.clamshellcli.api.Command;
import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * error 日志分析
 *
 * @author huyj
 * @Created 2018/6/20 19:36
 */
public class LogAnalysCmd implements Command {
    private static final Logger logger = LoggerFactory.getLogger(LogAnalysCmd.class);

    private static final String NAMESPACE = "dapeng";
    private static final String ACTION_NAME = "log";

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
                return "query the Error stack Info. ";
            }

            public String getUsage() {
                StringBuilder sb = new StringBuilder();
                sb.append(Configurator.VALUE_LINE_SEP).append(" log -date");
                sb.append(Configurator.VALUE_LINE_SEP).append(" log -hostname");
                sb.append(Configurator.VALUE_LINE_SEP).append(" log -sessiontid");
                sb.append(Configurator.VALUE_LINE_SEP).append(" log -threadpool");
                sb.append(Configurator.VALUE_LINE_SEP).append(" log -tag");
                sb.append(Configurator.VALUE_LINE_SEP).append(" log -slogtime");
                sb.append(Configurator.VALUE_LINE_SEP).append(" log -elogtime");
                return sb.toString();
            }

            Map<String, String> args = null;

            public Map<String, String> getArguments() {
                if (args != null) return args;
                args = new LinkedHashMap();
                args.put(CmdProperties.KEY_ARGS_DATE, "type '-date [2018.02.16]' to query the date error log.....");

                args.put(CmdProperties.KEY_ARGS_HOSTNAME, "type '-hostname ' to filter the log by hostname .....");
                args.put(CmdProperties.KEY_ARGS_SESSIONTID, "type '-sessiontid ' to filter the log by sessiontid .....");
                args.put(CmdProperties.KEY_ARGS_THREADPOOL, "type '-threadpool ' to filter the log by threadpool .....");
                args.put(CmdProperties.KEY_ARGS_TAG, "type '-tag ' to filter the log by tag .....");
                args.put(CmdProperties.KEY_ARGS_SLOGTIME, "type '-slogtime ' to query log after the time .....");
                args.put(CmdProperties.KEY_ARGS_ELOGTIME, "type '-elogtime ' to query log before the time .....");

                args.put(CmdProperties.KEY_ARGS_FILE_OUT, "type '-o ' to write data to file.");
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
        String args_date = inputArgs.get(CmdProperties.KEY_ARGS_DATE);
        String file_out = inputArgs.get(CmdProperties.KEY_ARGS_FILE_OUT);
        // query param
        //String args_level
        String args_hostname = inputArgs.get(CmdProperties.KEY_ARGS_HOSTNAME);
        String args_sessionTid = inputArgs.get(CmdProperties.KEY_ARGS_SESSIONTID);
        String args_tag = inputArgs.get(CmdProperties.KEY_ARGS_TAG);
        String args_threadPool = inputArgs.get(CmdProperties.KEY_ARGS_THREADPOOL);
        String args_slogtime = inputArgs.get(CmdProperties.KEY_ARGS_SLOGTIME);
        String args_elogtime = inputArgs.get(CmdProperties.KEY_ARGS_ELOGTIME);

        List<Map> listParamMap = new ArrayList<>();

        if (!CmdUtils.isEmpty(args_date)) {

            listParamMap.add(putMap(null, "term", putMap(null, "level", "ERROR")));
            if (!CmdUtils.isEmpty(args_hostname))
                listParamMap.add(putMap(null, "term", putMap(null, "hostname", args_hostname)));
            if (!CmdUtils.isEmpty(args_sessionTid))
                listParamMap.add(putMap(null, "term", putMap(null, "sessionTid", args_sessionTid)));
            if (!CmdUtils.isEmpty(args_tag)) listParamMap.add(putMap(null, "term", putMap(null, "tag", args_tag)));
            if (!CmdUtils.isEmpty(args_threadPool))
                listParamMap.add(putMap(null, "term", putMap(null, "threadPool", args_threadPool)));

            if (!CmdUtils.isEmpty(args_slogtime) || !CmdUtils.isEmpty(args_elogtime)) {
                Map timeMap = new HashMap();
                if (!CmdUtils.isEmpty(args_slogtime)) putMap(timeMap, "gt", args_slogtime);
                if (!CmdUtils.isEmpty(args_elogtime)) putMap(timeMap, "lt", args_elogtime);

                listParamMap.add(putMap(null, "range", putMap(null, "logtime", timeMap)));
            }

            String errorLog = loadErrorLog(args_date, listParamMap);
            //-o
            if (!CmdUtils.isEmpty(file_out)) {
                ServiceUtils.writerFile(context, file_out, errorLog);
            } else {
                //print console
                CmdUtils.writeMsg(context, errorLog);
            }
            handled = true;
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
