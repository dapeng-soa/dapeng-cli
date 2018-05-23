package com.github.dapeng.plugins;

import com.github.dapeng.openapi.cache.ServiceCache;
import com.github.dapeng.router.Route;
import com.github.dapeng.router.RoutesExecutor;
import com.github.dapeng.utils.CmdProperties;
import com.github.dapeng.utils.CmdUtils;
import com.github.dapeng.utils.ServiceUtils;
import com.github.dapeng.utils.ZookeeperUtils;
import org.clamshellcli.api.Command;
import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.dapeng.utils.CmdProperties.*;

public class ServiceCmd implements Command {
    private static final Logger logger = LoggerFactory.getLogger(ServiceCmd.class);
    private static final String NAMESPACE = "syscmd";
    private static final String ACTION_NAME = "service";

    @Override
    public Descriptor getDescriptor() {
        return new Command.Descriptor() {
            public String getNamespace() {
                return NAMESPACE;
            }

            public String getName() {
                return ACTION_NAME;
            }

            public String getDescription() {
                return "set/show the service info... ";
            }

            public String getUsage() {
                StringBuilder sb = new StringBuilder();
                sb.append(Configurator.VALUE_LINE_SEP).append(" service   -list  ");
                sb.append(Configurator.VALUE_LINE_SEP).append(" service   -runtime  ");
                sb.append(Configurator.VALUE_LINE_SEP).append(" service   -route  ");
                sb.append(Configurator.VALUE_LINE_SEP).append(" service   -config  ");
                sb.append(Configurator.VALUE_LINE_SEP).append(" service   -whitelist  ");
                sb.append(Configurator.VALUE_LINE_SEP).append(" service   -method  ");
                return sb.toString();
            }

            Map<String, String> args = null;

            public Map<String, String> getArguments() {
                if (args != null) return args;
                args = new LinkedHashMap();
                args.put(CmdProperties.KEY_ARGS_LIST, "type '-list ' to get runtime service list.");
                args.put(CmdProperties.KEY_ARGS_RUNTIME, "type '-runtime ' to get runtime instances.");
                args.put(CmdProperties.KEY_ARGS_ROUTE, "type '-route ' to get runtime service route info.");
                args.put(CmdProperties.KEY_ARGS_CONFIG, "type '-config ' to get runtime service config info.");
                args.put(CmdProperties.KEY_ARGS_WHITELIST, "type '-whitelist ' to get gateway service whitelist list.");
                args.put(CmdProperties.KEY_ARGS_METHOD, "type '-method ' to get runtime service mthods.");

                args.put(CmdProperties.KEY_ARGS_FILE_READ, "type '-f ' to read data from file.");
                args.put(CmdProperties.KEY_ARGS_FILE_OUT, "type '-o ' to write data to file.");
                args.put(CmdProperties.KEY_ARGS_DATA, "type '-d ' to set the data.");
                return args;
            }
        };
    }

    /**
     * 1. 获取运行时服务列表    service -l
     *
     * @param context
     * @return
     */
    @Override
    public Object execute(Context context) {
        boolean handled = false;
        // Map<String, String> inputArgs = CmdUtils.getCmdArgs(context);
        Map<String, String> inputArgs = CmdUtils._getCmdArgs(context);

        String args_list = inputArgs.get(CmdProperties.KEY_ARGS_LIST);
        String args_runtime = inputArgs.get(CmdProperties.KEY_ARGS_RUNTIME);
        String args_route = inputArgs.get(CmdProperties.KEY_ARGS_ROUTE);
        String args_config = inputArgs.get(CmdProperties.KEY_ARGS_CONFIG);
        String args_whitelist = inputArgs.get(CmdProperties.KEY_ARGS_WHITELIST);
        String args_method = inputArgs.get(CmdProperties.KEY_ARGS_METHOD);

        String args_data = inputArgs.get(CmdProperties.KEY_ARGS_DATA);
        String file_out = inputArgs.get(CmdProperties.KEY_ARGS_FILE_OUT);
        String file_read = inputArgs.get(CmdProperties.KEY_ARGS_FILE_READ);

        logger.info("[execute] ==>inputArgs=[{}]", inputArgs);
        //************************【获取服务列表 service -list】****************
        if (!CmdUtils.isEmpty(args_list)) {
            List<String> services = getRuntimeService();
            logger.info("[execute] ==>services=[{}]", services);

            //-o
            if (!CmdUtils.isEmpty(file_out)) {
                ServiceUtils.writerFile(context, file_out, CmdUtils.getResult(services));
                handled = true;
            } else {
                //print console
                CmdUtils.writeMsg(context, CmdUtils.getResult(services));
                handled = true;
            }
        }

        //************************【获取服务列表 service -runtime】****************
        if (!CmdUtils.isEmpty(args_runtime)) {
            String opt_path = RUNTIME_PATH + "/" + args_runtime;
            List<String> instances = ZookeeperUtils.getChildren(opt_path);

            //-o
            if (!CmdUtils.isEmpty(file_out)) {
                ServiceUtils.writerFile(context, file_out, CmdUtils.getResult(instances));
                handled = true;
            } else {  //print console
                CmdUtils.writeMsg(context, "get the instances: " + Configurator.VALUE_LINE_SEP + CmdUtils.getResult(instances));
                handled = true;
            }

            if (!CmdUtils.isEmpty(args_data) || !CmdUtils.isEmpty(file_read)) {
                CmdUtils.writeMsg(context, opt_path + " is protected.. it can not be setting.");
                handled = true;
            }
        }


        //************************【获取服务白名单列表 service -whitelist】****************
        if (!CmdUtils.isEmpty(args_whitelist)) {
            String opt_path = WHITELIST_PATH;
            List<String> whitelist = ZookeeperUtils.getChildren(opt_path);

            //-o
            if (!CmdUtils.isEmpty(file_out)) {
                ServiceUtils.writerFile(context, file_out, CmdUtils.getResult(whitelist));
                handled = true;
            } else {  //print console
                CmdUtils.writeMsg(context, "get the whitelist: " + Configurator.VALUE_LINE_SEP + CmdUtils.getResult(whitelist));
                handled = true;
            }

            if (!CmdUtils.isEmpty(args_data) || !CmdUtils.isEmpty(file_read)) {
                CmdUtils.writeMsg(context, "Setting up service whitelist on [" + opt_path + "]");
                String setNode = null;
                List<String> setNodes = null;
                if (!CmdUtils.isEmpty(file_read)) setNodes = ServiceUtils.readFromeFile2List(file_read);
                if (!CmdUtils.isEmpty(args_data)) setNode = args_data;

                if (null != setNodes){
                    setNodes.forEach(node -> {
                        ZookeeperUtils.createPath(WHITELIST_PATH + "/" + node, false);
                    });
                }else if (null != setNode){
                    ZookeeperUtils.createPath(WHITELIST_PATH + "/" + setNode,false);
                }

                CmdUtils.writeMsg(context, "Setting service whitelist successful");

                handled = true;
            }
        }


        //************************【获取服务列表 service -route | -config】****************
        if (!CmdUtils.isEmpty(args_route) || !CmdUtils.isEmpty(args_config)) {
            String opt_path = "";
            if (!CmdUtils.isEmpty(args_route)) opt_path = ROUTE_PATH + "/" + args_route;
            if (!CmdUtils.isEmpty(args_config)) opt_path = CONFIG_PATH + "/" + args_config;
            String data = ZookeeperUtils.getData(opt_path);

            //-o
            if (!CmdUtils.isEmpty(file_out)) {
                ServiceUtils.writerFile(context, file_out, data);
            } /*else {
                CmdUtils.writeMsg(context, "get the data: " + Configurator.VALUE_LINE_SEP + CmdUtils.getResult(data));
            }*/
            //-f,-d
            if (!CmdUtils.isEmpty(file_read) || !CmdUtils.isEmpty(args_data)) {
                String setData = null;
                if (!CmdUtils.isEmpty(file_read)) setData = ServiceUtils.readFromeFile(file_read);
                if (!CmdUtils.isEmpty(args_data)) setData = args_data;

                if (!CmdUtils.isEmpty(args_route)) {
                    //检查路由语法格式
                    logger.info("[execute] ==>routeData=[{}]", setData);
                    List<Route> routes = null;
                    try {
                        routes = RoutesExecutor.parseAll(setData);
                    } catch (Exception e) {
                        logger.info("parse route failed ...route configuration format is incorrect. exception:{}", e.getMessage());
                        //e.printStackTrace();
                    }
                    if (routes != null && !routes.isEmpty()) {
                        ZookeeperUtils.setData(context, opt_path, setData);
                    } else {
                        CmdUtils.writeMsg(context, "set route data failed. please confirm  route configuration format is incorrect...");
                    }
                } else {
                    ZookeeperUtils.setData(context, opt_path, setData);
                }
            }

            //print console
            if (CmdUtils.isEmpty(args_data) && CmdUtils.isEmpty(file_out) && CmdUtils.isEmpty(file_read)) {
                CmdUtils.writeMsg(context, "get the data: " + Configurator.VALUE_LINE_SEP + CmdUtils.getResult(data));
            }
            handled = true;
        }

        //************************【获取服务列表 service -method】****************
        if (!CmdUtils.isEmpty(args_method)) {
            List<String> mothods = CmdUtils.getRuntimeServiceMethods(context, args_method);
            //-o
            if (!CmdUtils.isEmpty(file_out)) {
                ServiceUtils.writerFile(context, file_out, CmdUtils.getResult(mothods));
            } else {
                //print console
                CmdUtils.writeMsg(context, "get the service methods : " + Configurator.VALUE_LINE_SEP + CmdUtils.getResult(mothods));
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


    private List<String> getRuntimeService() {
        logger.info("[getRuntimeService] ==>!ZookeeperUtils.isContextInitialized()=[{}]", !ZookeeperUtils.isContextInitialized());
        if (!ZookeeperUtils.isContextInitialized()) {
            ZookeeperUtils.connect();
        }
        logger.info("[getRuntimeService] ==>ServiceCache.getServices()=[{}]", ServiceCache.getServices());
        //List<String> services = ZookeeperUtils.getRuntimeServices();
        List<String> services = ServiceCache.getServices().entrySet().stream().map(i -> i.getValue().getNamespace() + "." + i.getKey()).collect(Collectors.toList());
        //Collections.sort(services);
        services.sort(String::compareToIgnoreCase);
        return services;
    }


}
