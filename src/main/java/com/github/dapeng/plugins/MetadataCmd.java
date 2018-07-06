package com.github.dapeng.plugins;

import com.github.dapeng.utils.CmdProperties;
import com.github.dapeng.utils.CmdUtils;
import com.github.dapeng.utils.ServiceUtils;
import org.clamshellcli.api.Command;
import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;

import java.util.LinkedHashMap;
import java.util.Map;

public class MetadataCmd implements Command {

    private static final String NAMESPACE = "syscmd";
    private static final String ACTION_NAME = "metadata";

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
                return " Get metadata xml.";
            }

            public String getUsage() {
                StringBuilder sb = new StringBuilder();
                sb.append(Configurator.VALUE_LINE_SEP)
                        .append(" metadata -s com.today.AdminService -v 1.0.0  ")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(" metadata -s com.today.AdminService -v 1.0.0 -o /tmp/com.today.AdminService.xml ")
                        .append(Configurator.VALUE_LINE_SEP);
//                        .append("metadata [options]").append(Configurator.VALUE_LINE_SEP);
//
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
                args.put(CmdProperties.KEY_ARGS_FILE_OUT, "optional: type '-o file(path + fileName)' to generate xml file, otherwise only return metadata string.. ");
                return args;
            }
        };
    }

    @Override
    public Object execute(Context context) {
        Map<String, String> inputArgs = CmdUtils.getCmdArgs(context);

        String serviceName = inputArgs.get(CmdProperties.KEY_ARGS_SERVICE);
        String file_out = inputArgs.get(CmdProperties.KEY_ARGS_FILE_OUT);
        String version = inputArgs.get(CmdProperties.KEY_ARGS_VERSION);

        CmdUtils.writeMsg(context, "inputParams: serviceName: " + serviceName + " version: " + version + " filePath: " + file_out);

        //2. 获取服务元数据
        if (serviceName != null && version != null) {
            String data = ServiceUtils.getMetadata(serviceName, version);
            if (file_out != null) {
                ServiceUtils.writerFile(context, file_out, data);
                //CmdUtils.writeMsg(context, file_out + " has generated successfully.....");
            } else {
                CmdUtils.writeMsg(context, data);
            }
        } else {
            String usage = getDescriptor().getUsage();
            CmdUtils.writeMsg(context, usage);
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
