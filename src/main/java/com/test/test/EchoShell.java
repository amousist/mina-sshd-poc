package com.test.test;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class EchoShell extends CommandExecutionHelper {
    public EchoShell() {
        super();
    }

    @Override
    protected boolean handleCommandLine(String command) throws Exception {
        OutputStream out = getOut();
        out.write((command + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();

        return !"exit".equals(command);

    }
}
