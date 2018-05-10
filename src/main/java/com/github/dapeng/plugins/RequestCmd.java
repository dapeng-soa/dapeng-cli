package com.github.dapeng.plugins;

import com.github.dapeng.metadata.MetadataClient;
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
import java.util.Map;

public class RequestCmd implements Command {
    private static final Logger logger = LoggerFactory.getLogger(RequestCmd.class);
    private static final String NAMESPACE = "syscmd";
    private static final String ACTION_NAME = "request";
    private static boolean isInitialized = false;

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
                return " Call runtime service.";
            }

            public String getUsage() {
                StringBuilder sb = new StringBuilder();
                sb.append(Configurator.VALUE_LINE_SEP)
                        .append(" request -s com.today.AdminService -v 1.0.0 -m method ")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(" request -s com.today.AdminService -v 1.0.0 -m method -f /tmp/com.today.AdminService.xml ")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(" request -metadata serviceName version")
                        .append(Configurator.VALUE_LINE_SEP);
                //.append("request [options]").append(Configurator.VALUE_LINE_SEP);

//                for(Map.Entry<String,String> entry : getArguments().entrySet()){
//
//                    sb.append(String.format("%n%1$5s", entry.getKey()) + "        " + entry.getValue());
//                }
//
//                sb.append(Configurator.VALUE_LINE_SEP);
                return sb.toString();
            }

            Map<String, String> args = null;

            public Map<String, String> getArguments() {
                if (args != null) return args;
                args = new LinkedHashMap<String, String>();
                args.put(CmdProperties.KEY_ARGS_SERVICE, "type '-s service' to specific service(package + serviceName).");
                args.put(CmdProperties.KEY_ARGS_VERSION, "type '-v serviceVersion' to specific serviceVersion.. ");
                args.put(CmdProperties.KEY_ARGS_SERVICE_METHOD, "type '-m method' to specific service method.. ");
                args.put(CmdProperties.KEY_ARGS_FILE, "type '-f file(path + fileName)' to get request json content for invoking..");
                args.put(CmdProperties.KEY_ARGS_METADATA, "type '-metadata serviceName version' to get metadata json content; you can type -f file(path + fileName) to save the metadata.");
                return args;
            }
        };
    }

    @Override
    public Object execute(Context context) {
        Map<String, String> inputArgs = CmdUtils.getCmdArgs(context);
        logger.info("[execute] ==>inputArgs={}",inputArgs);

        String serviceName = inputArgs.get(CmdProperties.KEY_ARGS_SERVICE);
        String fileName = inputArgs.get(CmdProperties.KEY_ARGS_FILE);
        String version = inputArgs.get(CmdProperties.KEY_ARGS_VERSION);
        String method = inputArgs.get(CmdProperties.KEY_ARGS_SERVICE_METHOD);

        String metadataObj = inputArgs.get(CmdProperties.KEY_ARGS_METADATA);

        if (serviceName != null && version != null && method != null && fileName != null) {

            if (!ZookeeperUtils.isContextInitialized()) {
                ZookeeperUtils.connect();
            }

            String jsonParams = ServiceUtils.readFromeFile(fileName);

            String result = ServiceUtils.post(serviceName, version, method, jsonParams);
            CmdUtils.writeMsg(context, result);
        }

        if (metadataObj != null) {

            String[] mdArr = metadataObj.split("#");
            logger.info("[execute] ==>mdArr={}",mdArr);
            try {
                String jsonResponse  = new MetadataClient(mdArr[0], mdArr[1]).getServiceMetadata();
                if (!CmdUtils.isEmpty(fileName)) {
                    ServiceUtils.writerFile(fileName, jsonResponse);
                    CmdUtils.writeMsg(context, "The metadata has been saved "+fileName + "is generated . ");
                } else {
                    CmdUtils.writeMsg(context,jsonResponse) ;
                }

            } catch (Exception e) {
                //e.printStackTrace();
                CmdUtils.writeMsg(context, "request -metadata error..[ex:"+e.getMessage()+"]");
            }
        }

        return null;
    }

    @Override
    public void plug(Context context) {

    }

    @Override
    public void unplug(Context context) {

    }
}
