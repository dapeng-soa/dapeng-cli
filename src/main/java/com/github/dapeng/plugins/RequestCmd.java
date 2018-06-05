package com.github.dapeng.plugins;

import com.github.dapeng.echo.EchoClient;
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
                        .append(" request -metadata serviceName -v 1.0.0")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(" request -echo serviceName -v 1.0.0")
                        .append(Configurator.VALUE_LINE_SEP);
                return sb.toString();
            }

            Map<String, String> args = null;

            public Map<String, String> getArguments() {
                if (args != null) return args;
                args = new LinkedHashMap<String, String>();
                args.put(CmdProperties.KEY_ARGS_SERVICE, "type '-s service' to specific service(package + serviceName).");
                args.put(CmdProperties.KEY_ARGS_VERSION, "type '-v serviceVersion' to specific serviceVersion.");
                args.put(CmdProperties.KEY_ARGS_SERVICE_METHOD, "type '-m method' to specific service method.");
                args.put(CmdProperties.KEY_ARGS_FILE_READ, "type '-f file(path + fileName)' to get request json content from file.");
                args.put(CmdProperties.KEY_ARGS_FILE_OUT, "type '-o file(path + fileName)' to save data to file.");
                args.put(CmdProperties.KEY_ARGS_METADATA, "type '-metadata serviceName' to get metadata json content.");
                args.put(CmdProperties.KEY_ARGS_ECHO, "type '-echo serviceName' to get echo content.");
                return args;
            }
        };
    }

    @Override
    public Object execute(Context context) {
        Map<String, String> inputArgs = CmdUtils.getCmdArgs(context);
        logger.info("[execute] ==>inputArgs={}", inputArgs);
        boolean handled = false;

        String serviceName = inputArgs.get(CmdProperties.KEY_ARGS_SERVICE);
        String file_read = inputArgs.get(CmdProperties.KEY_ARGS_FILE_READ);
        String file_out = inputArgs.get(CmdProperties.KEY_ARGS_FILE_OUT);
        String version = inputArgs.get(CmdProperties.KEY_ARGS_VERSION);
        String method = inputArgs.get(CmdProperties.KEY_ARGS_SERVICE_METHOD);
        String metadataData = inputArgs.get(CmdProperties.KEY_ARGS_METADATA);
        String echo = inputArgs.get(CmdProperties.KEY_ARGS_ECHO);

        if (serviceName != null && version != null && method != null && file_read != null) {

            if (!ZookeeperUtils.isContextInitialized()) {
                ZookeeperUtils.connect();
            }

            String jsonParams = ServiceUtils.readFromeFile(file_read);

            String result = ServiceUtils.post(serviceName, version, method, jsonParams);
            if (!CmdUtils.isEmpty(file_out)) {
                ServiceUtils.writerFile(context, file_out, result);
                //CmdUtils.writeMsg(context, "The metadata has been saved "+file_out + "is generated . ");
            } else {
                CmdUtils.writeMsg(context, result);
            }

            //CmdUtils.writeMsg(context, result);
            handled = true;
        }

        if (!CmdUtils.isEmpty(metadataData) && !CmdUtils.isEmpty(version)) {
            logger.info("[execute] ==>metadataData={},version=[{}]", metadataData, version);
            try {
                String jsonResponse = new MetadataClient(metadataData, version).getServiceMetadata();
                if (!CmdUtils.isEmpty(file_out)) {
                    ServiceUtils.writerFile(context, file_out, jsonResponse);
                    //CmdUtils.writeMsg(context, "The metadata has been saved "+file_out + "is generated . ");
                } else {
                    CmdUtils.writeMsg(context, jsonResponse);
                }

            } catch (Exception e) {
                //e.printStackTrace();
                CmdUtils.writeMsg(context, "request -metadata error..[ex:" + e.getMessage() + "]");
            }
            handled = true;
        }

        if (!CmdUtils.isEmpty(echo) && !CmdUtils.isEmpty(version)) {
            logger.info("[execute] ==>echo={},version=[{}]", echo, version);
            try {
                String jsonResponse = new EchoClient(echo, version).echo();
                if (!CmdUtils.isEmpty(file_out)) {
                    ServiceUtils.writerFile(context, file_out, jsonResponse);
                    //CmdUtils.writeMsg(context, "The metadata has been saved "+file_out + "is generated . ");
                } else {
                    CmdUtils.writeMsg(context, jsonResponse);
                }

            } catch (Exception e) {
                //e.printStackTrace();
                CmdUtils.writeMsg(context, "request -echo error..[ex:" + e.getMessage() + "]");
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
}
