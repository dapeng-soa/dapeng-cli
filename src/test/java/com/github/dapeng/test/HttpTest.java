package com.github.dapeng.test;

import com.github.dapeng.utils.HttpUtils;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huyj
 * @Created 2018/6/20 17:19
 */
public class HttpTest {
    private static final String jsonParam = "{\n" +
            "   \"query\": {\n" +
            "      \"bool\": {\n" +
            "         \"must\": [\n" +
            "           {\n" +
            "             \"term\": {\n" +
            "                  \"level\": \"ERROR\"\n" +
            "               }\n" +
            "           }\n" +
            "           \n" +
            "         ], \n" +
            "         \n" +
            "         \"filter\": {\n" +
            "            \"script\": {\n" +
            "               \"script\": {\n" +
            "                  \"source\": \"doc['message'].values.length==0\"\n" +
            "               }\n" +
            "            }\n" +
            "         }\n" +
            "      }\n" +
            "   },\n" +
            "   \"from\": 0,\n" +
            "\t\"size\":#{total}\n" +
            "}";

    public static void main(String[] arg0) {

        String date = "2018.07.05";

        List<Map> listParam = new ArrayList<>();
        listParam.add(putMap(null, "term", putMap(null, "level", "ERROR")));
        // listParam.add(putMap(null, "term", putMap(null, "tag", "apiGateWay")));
        //listParam.add(putMap(null, "term", putMap(null, "hostname", "192.168.10.125")));
       // listParam.add(putMap(null, "term", putMap(null, "sessionTid", "ac19000404c4ef77")));

        Map timeMap = new HashMap();
        putMap(timeMap, "gt", "07-05 16:42:01 121");
        putMap(timeMap, "lt", "07-05 16:44:00 126");
        listParam.add(putMap(null, "range", putMap(null, "logtime", timeMap)));


        //listParam.add(putMap(null, "term", putMap(null, "logtime", "06-21 00:00:01 398")));

        System.out.println(loadErrorLog(date, listParam));

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
