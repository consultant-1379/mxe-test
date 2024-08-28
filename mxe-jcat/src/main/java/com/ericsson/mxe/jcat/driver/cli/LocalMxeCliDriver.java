package com.ericsson.mxe.jcat.driver.cli;

import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import com.ericsson.mxe.jcat.command.CustomCommand;
import com.ericsson.mxe.jcat.command.result.CommandResult;
import com.ericsson.mxe.jcat.config.TestExecutionHost;

public class LocalMxeCliDriver extends MxeCliDriver {

    private final ProcessExecutor processExecutor;

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalMxeCliDriver.class);

    public LocalMxeCliDriver(final TestExecutionHost testExecutionHost) {
        super(testExecutionHost);
        processExecutor = new ProcessExecutor().readOutput(true).destroyOnExit();
    }

    @Override
    public CommandResult execute(CustomCommand command) {
        CommandResult commandResult = null;
        try {
            LOGGER.info("<b>[command]</b><br/>{}", command.getSyntax());
            final ProcessResult processResult = processExecutor.command(command.getSyntaxAsList()).execute();
            commandResult = new CommandResult(processResult.outputUTF8().trim(), processResult.getExitValue());
            LOGGER.info("<b>[result]</b><br/>{}", commandResult.getCommandOutput());
            LOGGER.info("<b>[exit code]</b><br/>{}", commandResult.getExitCode());
        } catch (InterruptedException e) {
            LOGGER.error("Process was interrupted", e);
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            LOGGER.error("Timeout occurred", e);
        } catch (Exception e) {
            LOGGER.error("Could not execute command", e);
        }

        return commandResult;
    }

    @Override
    public void close() throws Exception {
        // Intentionally left blank
    }
}
