package com.github.dapeng.plugins;

import com.github.dapeng.utils.CmdProperties;
import com.github.dapeng.utils.CmdUtils;
import com.github.dapeng.utils.ServiceUtils;
import org.clamshellcli.api.Command;
import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonCmd implements Command {


    private static final String NAMESPACE = "syscmd";
    private static final String ACTION_NAME = "json";

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
                return " Get Json request format.";
            }

            public String getUsage() {
                StringBuilder sb = new StringBuilder();
                sb.append(Configurator.VALUE_LINE_SEP)
                        .append(" json -s service -v version -m method")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(" json -s service -v version -m method -f fileName")
                        .append(Configurator.VALUE_LINE_SEP)
                        .append(" json -s com.today.UserService -v 1.0.0 -m createUser ")
                        .append(Configurator.VALUE_LINE_SEP);
//                        .append("json [options]").append(Configurator.VALUE_LINE_SEP);
//
//                for(Map.Entry<String,String> entry : getArguments().entrySet()){
//
//                    sb.append(String.format("%n%1$5s", entry.getKey()) + "        " + entry.getValue());
//                }
//
//                sb.append(Configurator.VALUE_LINE_SEP);
                return sb.toString();
            }

            Map<String,String> args = null;
            public Map<String, String> getArguments() {
                if(args != null) return args;
                args = new LinkedHashMap<String,String>();
                args.put(CmdProperties.KEY_ARGS_SERVICE , "[required] type '-s service' to specific service(package + serviceName).");
                args.put(CmdProperties.KEY_ARGS_VERSION , "[required] type '-v serviceVersion' to specific serviceVersion.. ");
                args.put(CmdProperties.KEY_ARGS_SERVICE_METHOD, "[required] type '-m mehtodName' to specific method");
                args.put(CmdProperties.KEY_ARGS_FILE , "[optional] type '-f file(path + fileName)' to generate xml file, otherwise only return metadata string.. ");
                return args;
            }
        };
    }

    @Override
    public Object execute(Context context) {

        Map<String, String> inputArgs = CmdUtils.getCmdArgs(context);
        String sName = inputArgs.get(CmdProperties.KEY_ARGS_SERVICE);
        String sVersion = inputArgs.get(CmdProperties.KEY_ARGS_VERSION);
        String sMethod = inputArgs.get(CmdProperties.KEY_ARGS_SERVICE_METHOD);
        String fileName = inputArgs.get(CmdProperties.KEY_ARGS_FILE);

        //3. 获取Json格式
        if (CmdUtils.isEmpty(sName) || CmdUtils.isEmpty(sVersion) || CmdUtils.isEmpty(sMethod)) {

            CmdUtils.writeMsg(context, " request format is invalid.. please check your input.....");
            String usage = getDescriptor().getUsage();
            CmdUtils.writeMsg(context, usage);

        } else {
            String jsonRequestSample = ServiceUtils.getJsonRequestSample(sName,sVersion,sMethod);

            if (!CmdUtils.isEmpty(fileName)) {
                ServiceUtils.writerFile(fileName, jsonRequestSample);
                CmdUtils.writeMsg(context, fileName + "is generated . ");
            } else {
                CmdUtils.writeMsg(context, jsonRequestSample);
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
