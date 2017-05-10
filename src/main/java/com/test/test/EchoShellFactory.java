package com.test.test;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;


public class EchoShellFactory implements Factory<Command> {
    public static final EchoShellFactory INSTANCE = new EchoShellFactory();

    public EchoShellFactory() {
        super();
    }

    @Override
    public Command create() {
        return new EchoShell();
    }
}
