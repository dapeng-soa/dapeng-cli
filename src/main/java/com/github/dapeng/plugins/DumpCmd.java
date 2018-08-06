package com.github.dapeng.plugins;

import com.github.dapeng.utils.CmdProperties;
import com.github.dapeng.utils.CmdUtils;
import com.github.dapeng.utils.DumpUtils;
import org.clamshellcli.api.Command;
import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * 获取共享内存的限流数据
 * @author hui
 * @date 2018/8/1 0001 17:03
 */
public class DumpCmd implements Command {
    private static final Logger logger = LoggerFactory.getLogger(DumpCmd.class);
    private static final String NAMESPACE = "syscmd";
    private static final String ACTION_NAME = "dumpmem";



    @Override
    public Descriptor getDescriptor() {
        return new  Command.Descriptor(){

            @Override
            public String getNamespace() {
                return NAMESPACE;
            }

            @Override
            public String getName() {
                return ACTION_NAME;
            }

            @Override
            public String getDescription() {
                return "show the data of freqControl in the share memory.";
            }

            @Override
            public String getUsage() {
                StringBuilder result = new StringBuilder();
                result.append(Configurator.VALUE_LINE_SEP).append(" method -f /data/shm.data");
                return result.toString();
            }

            Map<String,String> args = null;
            @Override
            public Map<String, String> getArguments() {
                if (args != null) return args;
                args = new LinkedHashMap();
                args.put(CmdProperties.KEY_ARGS_FILE_READ, "type '-f ' to read data from file.");
                return args;
            }
        };
    }

    @Override
    public Object execute(Context context) {
        Map<String, String> inputArgs = CmdUtils._getCmdArgs(context);
        String file_read = inputArgs.get(CmdProperties.KEY_ARGS_FILE_READ);
        logger.info("[execute] ==>inputArgs=[{}]", inputArgs);
        if(!CmdUtils.isEmpty(file_read)){
            try {
                DumpUtils.getFreqData(file_read);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
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
