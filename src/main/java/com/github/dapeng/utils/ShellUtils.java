package com.github.dapeng.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author hui
 * 2019/12/9 0009 11:53
 */
public class ShellUtils {
    private static final Logger logger = LoggerFactory.getLogger(ShellUtils.class);

    public static void executeShellScriptFromFile(String scriptFile) {
        logger.info("execute Shell Script from file: " + scriptFile);
        try {
            StringBuilder command = new StringBuilder();

            List<String> lines = Files.readAllLines(Paths.get(scriptFile), StandardCharsets.UTF_8);

            for (String line : lines) {
                command.append(line).append(System.getProperty("line.separator"));
            }
            executeShellScript(command.toString());

        } catch (IOException e) {
            logger.error("fail to execute shell script" + e.getMessage());
        }
    }

    public static void executeShellScript(String command) {
        try {
            logger.info("command:" + command);
            String[] cmd = new String[]{"/bin/sh", "-c", command};

            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
