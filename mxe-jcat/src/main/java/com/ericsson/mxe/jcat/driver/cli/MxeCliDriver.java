package com.ericsson.mxe.jcat.driver.cli;

import com.ericsson.mxe.jcat.command.CustomCommand;
import com.ericsson.mxe.jcat.command.result.CommandResult;
import com.ericsson.mxe.jcat.config.TestExecutionHost;

public abstract class MxeCliDriver implements AutoCloseable {

    private TestExecutionHost testExecutionHost;

    public MxeCliDriver(final TestExecutionHost testExecutionHost) {
        this.testExecutionHost = testExecutionHost;
    }

    public abstract CommandResult execute(CustomCommand command);

    public TestExecutionHost getTestExecutionHost() {
        return testExecutionHost;
    }
}
