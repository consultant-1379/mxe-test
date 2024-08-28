package com.ericsson.mxe.jcat.test;

import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Supplier;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Parameters;

import com.ericsson.mxe.jcat.command.CustomCommand;
import com.ericsson.mxe.jcat.command.profile.CommandProfiles;
import com.ericsson.mxe.jcat.command.result.CommandResult;
import com.ericsson.mxe.jcat.config.Config;
import com.ericsson.mxe.jcat.config.HostOperatingSystemType;
import com.ericsson.mxe.jcat.config.HostType;
import com.ericsson.mxe.jcat.config.TestExecutionHost;
import com.ericsson.mxe.jcat.context.MxeJcatApplicationContextProvider;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.profile.TestExecutionHostProfiles;

import se.ericsson.jcat.fw.annotations.JcatMethod;
import se.ericsson.jcat.fw.fixture.testng.JcatNGSuiteListener;
import se.ericsson.jcat.fw.fixture.testng.JcatNGTestListener;
import se.ericsson.jcat.fw.testbase.JcatTestBaseFluent;

@Listeners({ JcatNGSuiteListener.class, JcatNGTestListener.class })
public class MxeTestBase extends JcatTestBaseFluent {

    protected static final String ERROR_EXIT_CODE_NOT_ZERO = "Command exit code was not 0";
    protected static final String ERROR_EXIT_CODE_ZERO = "Command exit code was 0";
    protected static final String HEADER_ELEMENT_NAME = "NAME";
    protected static final String ERROR_RESOURCE_RELEASE = "Could not release cli driver resources";

    protected TestExecutionHost testExecutionHost;

    @JcatMethod(testTag = "CONFIG-TEST-SETUP", testTitle = "Create configuration")
    @Parameters("host")
    @BeforeTest
    public void setup(@org.testng.annotations.Optional("localLinux") String host) throws IOException {
        final Config config = Config.getInstance();
        Optional<TestExecutionHost> optionalMxeHost = config.getMxeHost(host);

        if (optionalMxeHost.isPresent()) {
            testExecutionHost = optionalMxeHost.get();
        } else {
            fail("Failed to get MXE host");
        }

        final String testExecutionHostProfile = testExecutionHost.getHostType() == HostType.LOCAL ? TestExecutionHostProfiles.LOCAL
                : TestExecutionHostProfiles.REMOTE;
        final String commandProfile = testExecutionHost.getHostOperatingSystemType() == HostOperatingSystemType.LINUX ? CommandProfiles.LINUX
                : CommandProfiles.WINDOWS;

        MxeJcatApplicationContextProvider.setActiveProfiles(testExecutionHostProfile, commandProfile);
    }

    protected static void executeCommandInStep(final MxeCliDriver mxeCliDriver, final CustomCommand customCommand) {
        executeCommandInStep(mxeCliDriver, customCommand, customCommand.getSyntax());
    }

    protected static void executeCommandInStep(final MxeCliDriver mxeCliDriver, final CustomCommand customCommand, final String stepName) {
        setTestStepBegin(stepName);
        executeCommand(mxeCliDriver, customCommand);
        setTestStepEnd();
    }

    protected static Optional<CommandResult> executeCommand(final MxeCliDriver mxeCliDriver, final CustomCommand customCommand) {
        final CommandResult commandResult = mxeCliDriver.execute(customCommand);
        if (Objects.nonNull(commandResult)) {
            final int exitCode = commandResult.getExitCode();
            saveAssertThat(ERROR_EXIT_CODE_NOT_ZERO, exitCode, is(0));
        } else {
            saveFail("Command was not executed");
        }
        return Optional.ofNullable(commandResult);
    }

    protected static void executeInStep(final MxeCliDriver mxeCliDriver, final Supplier<CustomCommand> customCommandSupplier, final String stepName) {
        setTestStepBegin(stepName);
        CustomCommand customCommand = customCommandSupplier.get();
        executeCommand(mxeCliDriver, customCommand);
        setTestStepEnd();
    }

    protected static List<Map<String, String>> parseListCommandResult(CommandResult commandResult) {
        List<String> header = new ArrayList<>();
        List<Map<String, String>> listResult = new ArrayList<>();
        try (Scanner scanner = new Scanner(commandResult.getCommandOutput())) {
            if (scanner.hasNextLine()) {
                header = Arrays.asList(scanner.nextLine().split("\\s\\s+"));
            }
            while (scanner.hasNextLine()) {
                HashMap<String, String> lineMap = new HashMap<>();
                List<String> line = Arrays.asList(scanner.nextLine().split("\\s\\s+"));
                int i = 0;
                for (String headerElement : header) {
                    lineMap.put(headerElement, (line.size() > i ? line.get(i) : ""));
                    i++;
                }
                listResult.add(lineMap);
            }
        }
        return listResult;
    }
}
