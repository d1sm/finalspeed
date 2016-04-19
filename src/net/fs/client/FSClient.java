// Copyright (c) 2015 D1SM.net

package net.fs.client;

import org.apache.commons.cli.*;

public class FSClient {

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("b", "back", false, "有此参数则运行CLI版本");
        options.addOption("min", "minimize",false, "启动窗口最小化");
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("java -jar finalspeed.jar [-b/--back]", options);
            System.exit(0);
        }

        boolean visible=!commandLine.hasOption("b");
        boolean min=commandLine.hasOption("min");
        
        new ClientUI(visible,min);
    }
}
