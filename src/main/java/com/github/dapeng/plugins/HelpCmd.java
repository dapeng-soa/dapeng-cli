/*
 * Copyright 2012 ClamShell-Cli.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.dapeng.plugins;

import com.github.dapeng.utils.CmdUtils;
import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class implements the Help command.
 * <ul>
 * <li> Usage: help - displays description for all installed commands.
 * <li> Usage: help [command_name] displays command usage.
 * </ul>
 * @author vladimir.vivien
 */
public class HelpCmd implements Command{
    private static final Logger logger = LoggerFactory.getLogger(HelpCmd.class);
    private static final String NAMESPACE = "syscmd";
    private static final String CMD_NAME = "help";
    private HelpCmdDescriptor descriptor = new HelpCmdDescriptor();
    
    private class HelpCmdDescriptor implements Descriptor {
        @Override public String getNamespace() {return NAMESPACE;}

        @Override
        public String getName() {
            return CMD_NAME;
        }

        @Override
        public String getDescription() {
            return "Displays help information for available commands.";
        }

        @Override
        public String getUsage() {
            return "Type 'help' or 'help [command_name]' ";
        }

        @Override
        public Map<String, String> getArguments() {
            return Collections.emptyMap();

        }
    }

    @Override
    public Descriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Executes the Help command.
     * The help command expects format 'help [command_name]'.
     * If the optional command_name parameter is present, this class will
     * display info about about the command.  If command_name is not present
     * the Help command displays a list of help for all installed command.
     * @param ctx
     * @return
     */
    @Override
    public Object execute(Context ctx) {
        try {
            logger.info("[execute] ==> help command execute....");
            String[] args = (String[]) ctx.getValue(Context.KEY_COMMAND_LINE_ARGS);
            logger.info("[execute] ==> args ={},ctx.getIoConsole()={}",args,ctx.getIoConsole());
            // if arg passed, display help for command matching arg.
            if(args != null && args.length > 0){
                printCommandHelp(ctx, args[0].trim());
            }else{
                printAllHelp(ctx);
            }
            CmdUtils.writeFormatMsg(ctx,"%n%n");
        } catch (Exception e) {
            CmdUtils.writeFormatMsg(ctx," help error.....");
            logger.info(" failed to execute help" + e.getMessage());
        }
        return null;
    }

    @Override
    public void plug(Context plug) {
        // no plugin action needed
    }

    @Override
    public void unplug(Context plug){
        // nothing to do
    }

    private void printCommandHelp(Context ctx, String cmdName){
        Map<String, Command> commands = ctx.mapCommands(ctx.getCommands());
        logger.info("[printCommandHelp] ==>commands=[{}]",commands);
        if(commands != null){
            Command cmd = commands.get(cmdName.trim());
            if(cmd != null){
                printCommandHelp(ctx, cmd);
            }else{
                CmdUtils.writeFormatMsg(ctx,"%nUnable to find command [%s].", cmdName);
            }
        }
    }

    private void printCommandHelp(Context ctx, Command cmd){
        if(cmd != null && cmd.getDescriptor() != null){
            IOConsole io = ctx.getIoConsole();
            CmdUtils.writeFormatMsg(ctx,"%n@|bold,red Command:|@ %s - %s%n",
                cmd.getDescriptor().getName(),
                cmd.getDescriptor().getDescription()
            );
            CmdUtils.writeFormatMsg(ctx,"Usage: %s", cmd.getDescriptor().getUsage());
            printCommandParamsDetail(ctx, cmd);
        }else{
            CmdUtils.writeFormatMsg(ctx,"Unable to display help for command.");
        }
    }

    private void printCommandParamsDetail(Context ctx, Command cmd){
        Descriptor desc = cmd.getDescriptor();
        if(desc == null || desc.getArguments() == null) return;
        IOConsole c = ctx.getIoConsole();
        CmdUtils.writeFormatMsg(ctx,"%nOptions:");
        CmdUtils.writeFormatMsg(ctx,"%n--------");
        for(Map.Entry<String,String> entry : desc.getArguments().entrySet()){
            CmdUtils.writeFormatMsg(ctx,"%n%1$10s\t%2$s", entry.getKey(), entry.getValue());
        }
    }
    
    private void printAllHelp(Context ctx){
        IOConsole c = ctx.getIoConsole();
        CmdUtils.writeFormatMsg(ctx,"%nAvailable Commands");
        CmdUtils.writeFormatMsg(ctx,"%n------------------");
        List<Command> commands = ctx.getCommands();
        for(Command cmd : commands){
            CmdUtils.writeFormatMsg(ctx,"%n%1$10s %2$5s %3$s", cmd.getDescriptor().getName(), " ", cmd.getDescriptor().getDescription());
        }
    }
    
}
