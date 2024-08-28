package com.ericsson.mxe.jcat.driver.cli;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.commonlibrary.remotecli.Cli;
import com.ericsson.commonlibrary.remotecli.CliBuilder;
import com.ericsson.commonlibrary.remotecli.CliFactory;
import com.ericsson.commonlibrary.remotecli.exceptions.RemoteCliException;
import com.ericsson.mxe.jcat.command.CustomCommand;
import com.ericsson.mxe.jcat.command.result.CommandResult;
import com.ericsson.mxe.jcat.config.TestExecutionHost;

public class RemoteMxeCliDriver extends MxeCliDriver {

    private static final String NEW_LINE_REG_EX = "\n";
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteMxeCliDriver.class);

    private Cli cli;

    public RemoteMxeCliDriver(final TestExecutionHost testExecutionHost) {
        super(testExecutionHost);

        final CliBuilder builder = CliFactory.newSshBuilder().setHost(testExecutionHost.getHost()).setUsername(testExecutionHost.getUser().getUserName())
                .setPassword(testExecutionHost.getUser().getPassword()).setPort(testExecutionHost.getPort()).setConnectAutoFindPromptTimeoutMillis(60 * 1000)
                .setSendTimeoutMillis(60 * 1000).setSshPtySize(300, 24, 640, 480);

        builder.setNewline(NEW_LINE_REG_EX);

        cli = builder.build();
        cli.setExpectedRegexPrompt(testExecutionHost.getUser().getPrompt());
    }

    @Override
    public CommandResult execute(CustomCommand command) {
        CommandResult commandResult = null;

        try {
            if (Objects.isNull(cli)) {
                LOGGER.error("CLI not initialized, unable to send command {}", command.getSyntax());
                return null;
            }
            cli.connect();

            //TODO: add proper exit status

            commandResult = new CommandResult(cli.send(command.getSyntax()), 0);
        } catch (final RemoteCliException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (!Objects.isNull(cli)) {
                cli.disconnect();
            }
        }
        return commandResult;
    }

    @Override
    public void close() throws Exception {
        cli.disconnect();
    }
}
