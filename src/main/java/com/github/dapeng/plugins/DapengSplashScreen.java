package com.github.dapeng.plugins;

import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;
import org.clamshellcli.api.SplashScreen;

public class DapengSplashScreen implements SplashScreen {

    private StringBuilder screen;

    @Override
    public void render(Context ctx) {
        IOConsole console = ctx.getIoConsole();
        console.println(screen.toString());
        //console.writeOutput(screen.toString());
    }

    @Override
    public void plug(Context context) {
        screen = new StringBuilder(128);

        screen
        .append(Configurator.VALUE_LINE_SEP)
        .append(Configurator.VALUE_LINE_SEP)
        .append("                                                                                " ).append(Configurator.VALUE_LINE_SEP)
        .append(" 888888           88         88888888I    88888888  888      88     GGGGGGG      " ).append(Configurator.VALUE_LINE_SEP)
        .append(" 888   888      88  88       88      88   88        88 88    88   GG       GG    ").append(Configurator.VALUE_LINE_SEP)
        .append(" 888     88    88    88      88      88   88        88  88   88   GG             ").append(Configurator.VALUE_LINE_SEP)
        .append(" 888     88   88      88     88888888I    88888888  88   88  88   GG              ").append(Configurator.VALUE_LINE_SEP)
        .append(" 888     88  88 888888 88    88           88        88    88 88   GG     GGGGG    ").append(Configurator.VALUE_LINE_SEP)
        .append(" 88b   I8P  88          88   88           88        88     8 88   GG       GGG    ").append(Configurator.VALUE_LINE_SEP)
        .append(" Y8888P    88            88  88           88888888  88       88    GGGGGGGGG      ").append(Configurator.VALUE_LINE_SEP)
        .append(Configurator.VALUE_LINE_SEP)
        .append(Configurator.VALUE_LINE_SEP)
        .append("Java version: ").append(System.getProperty("java.version")).append(Configurator.VALUE_LINE_SEP)
        .append("Java Home: ").append(System.getProperty("java.home")).append(Configurator.VALUE_LINE_SEP)
        .append("OS: ").append(System.getProperty("os.name")).append(", Version: ").append(System.getProperty("os.version"))
        .append(Configurator.VALUE_LINE_SEP)
        .append(Configurator.VALUE_LINE_SEP)
        ;
        setScreen(screen);

    }

    @Override
    public void unplug(Context context) {

    }


    public StringBuilder getScreen() {
        return screen;
    }

    public void setScreen(StringBuilder screen) {
        this.screen = screen;
    }
}
