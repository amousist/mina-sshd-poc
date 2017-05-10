package com.test.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

public abstract class CommandExecutionHelper implements Command, Runnable, ExitCallback {
    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;
    private Environment environment;
    private Thread thread;
    private boolean cbCalled;
    // null/empty if shell
    private String command;

    protected CommandExecutionHelper() {
        this(null);
    }

    protected CommandExecutionHelper(String command) {
        this.command = command;
    }

    public InputStream getIn() {
        return in;
    }

    public OutputStream getOut() {
        return out;
    }

    public OutputStream getErr() {
        return err;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public ExitCallback getExitCallback() {
        return callback;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void start(Environment env) throws IOException {
        environment = env;
        thread = new Thread(this, "CommandExecutionHelper");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void destroy() {
        thread.interrupt();
    }

    @Override
    public void run() {
        try {
            if (command == null) {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(getIn(), StandardCharsets.UTF_8))) {
                    for (;;) {
                        command = r.readLine();
                        if (command == null) {
                            return;
                        }

                        if (!handleCommandLine(command)) {
                            return;
                        }
                    }
                }
            } else {
                handleCommandLine(command);
            }
        } catch (InterruptedIOException e) {
            // Ignore - signaled end
        } catch (Exception e) {
            String message = "Failed (" + e.getClass().getSimpleName() + ") to handle '" + command + "': " + e.getMessage();
            try {
                OutputStream stderr = getErr();
                stderr.write(message.getBytes(StandardCharsets.US_ASCII));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                onExit(-1, message);
            }
        } finally {
            onExit(0);
        }
    }

    @Override
    public void onExit(int exitValue, String exitMessage) {
        if (!cbCalled) {
            ExitCallback cb = getExitCallback();
            try {
                cb.onExit(exitValue, exitMessage);
            } finally {
                cbCalled = true;
            }
        }
    }

    /**
     * @param command The command line
     * @return {@code true} if continue accepting command
     * @throws Exception If failed to handle the command line
     */
    protected abstract boolean handleCommandLine(String command) throws Exception;
}
