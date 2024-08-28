package com.ericsson.mxe.jcat.test;

import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.ericsson.mxe.jcat.command.Commands;
import com.ericsson.mxe.jcat.command.CustomCommand;
import com.ericsson.mxe.jcat.command.MxeFlowCommand;
import com.ericsson.mxe.jcat.command.result.CommandResult;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;
import se.ericsson.jcat.fw.annotations.JcatClass;
import se.ericsson.jcat.fw.annotations.JcatMethod;

/**
 * @JcatDocChapterDescription Chapter covering flow function tests.
 */
@JcatClass(chapterName = "Flow Function Tests")
public class MxeFlowTest extends MxeTestBase {

    /**
     * @JcatTcDescription List MXE flow commands
     * @JcatTcPreconditions None
     * @JcatTcInstruction The testcase is about running the mxe-flow CLI to print list related help messages using default cluster
     * @JcatTcAction mxe-flow list --help --verbose
     * @JcatTcActionResult Help message is printed
     * @JcatTcAction mxe-flow list --verbose
     * @JcatTcActionResult Help message is printed
     * @JcatTcAction mxe-flow list --help
     * @JcatTcActionResult Help message is printed
     * @JcatTcAction mxe-flow list
     * @JcatTcActionResult Help message is printed
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-FLOW-COMMANDS", testTitle = "List MXE flow commands")
    public void testMxeFlowPrintHelp() {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            executeInStep(mxeCliDriver, () -> Commands.mxeFlow().list().help().verbose(), "Flow list help verbose");

            executeInStep(mxeCliDriver, () -> Commands.mxeFlow().list().verbose(), "Flow list verbose");

            executeInStep(mxeCliDriver, () -> Commands.mxeFlow().list().help(), "Flow list help");

            executeInStep(mxeCliDriver, () -> Commands.mxeFlow().list(), "Flow list");
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Onboard and deploy a flow on local cluster
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction Test MXE flow onboard and deploy commands
     * @JcatTcAction Onboard the flow
     * @JcatTcActionResult Flow is onboarded successfully
     * @JcatTcAction List onboarded flows
     * @JcatTcActionResult Previously onboarded flow appears in the deployed flow list
     * @JcatTcAction Deploy the onboarded flow
     * @JcatTcActionResult Flow deployed successfully
     * @JcatTcAction Check if flow deployment exists
     * @JcatTcActionResult Previously deployed flow appears in the deployed flows list
     * @JcatTcAction Delete flow deployment
     * @JcatTcActionResult Flow deployment deleted successfully
     * @JcatTcAction Check if flow deployment exists
     * @JcatTcActionResult Flow must not appear in the deployed flows list
     * @JcatTcAction Delete the flow
     * @JcatTcActionResult Flow deleted successfully
     * @JcatTcAction List onboarded flows
     * @JcatTcActionResult Flow must not appear in the deployed flow list
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-FLOW-ONBOARD-AND-DEPLOY", testTitle = "Test MXE flow onboard and deploy commands")
    @Parameters({"flowName", "flowFile", "flowDeploymentName"})
    public void testMxeFlowOnboardAndDeploy(String flowName, String flowFile, String flowDeploymentName) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            executeInStep(mxeCliDriver, () -> Commands.mxeFlow().onboard(flowFile, flowName),
                    "Onboard flow '" + flowName + "'");

            checkIfFlowExists(mxeCliDriver, flowName);

            executeInStep(mxeCliDriver, () -> Commands.mxeFlow().deploy(flowDeploymentName, flowName),
                    "Deploy flow as '" + flowDeploymentName + "'");

            checkIfFlowDeploymentExists(mxeCliDriver, flowDeploymentName);

            executeInStep(mxeCliDriver, () -> Commands.mxeFlow().deleteDeployment(flowDeploymentName),
                    "Delete flow deployment '" + flowDeploymentName + "'");

            checkIfFlowDeploymentNotExists(mxeCliDriver, flowDeploymentName);

            executeInStep(mxeCliDriver, () -> Commands.mxeFlow().deleteFlow(flowName),
                    "Delete flow '" + flowName + "'");

            checkIfFlowNotExists(mxeCliDriver, flowName);


        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Onboard and deploy a flow on local cluster: negative cases
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction Test MXE flow onboard and deploy commands negative cases
     * @JcatTcAction Onboard the flow
     * @JcatTcActionResult Flow is onboarded successfully
     * @JcatTcAction List onboarded flows
     * @JcatTcActionResult Previously onboarded flow appears in the deployed flow list
     * @JcatTcAction Onboard the already onboarded flow
     * @JcatTcActionResult Flow onboard should fail
     * @JcatTcAction Deploy the onboarded flow
     * @JcatTcActionResult Flow deployed successfully
     * @JcatTcAction Check if flow deployment exists
     * @JcatTcActionResult Previously deployed flow appears in the deployed flows list
     * @JcatTcAction Deploy the already deployed flow
     * @JcatTcActionResult Flow deploy should fail
     * @JcatTcAction Delete flow deployment
     * @JcatTcActionResult Flow deployment deleted successfully
     * @JcatTcAction Check if flow deployment exists
     * @JcatTcActionResult Flow must not appear in the deployed flows list
     * @JcatTcAction Delete a non-existing flow deployment
     * @JcatTcActionResult Deleting should fail
     * @JcatTcAction Delete the flow
     * @JcatTcActionResult Flow deleted successfully
     * @JcatTcAction List onboarded flows
     * @JcatTcActionResult Flow must not appear in the deployed flow list
     * @JcatTcAction Delete a non-existing flow
     * @JcatTcActionResult Deleting should fail
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-FLOW-ONBOARD-AND-DEPLOY-NEGATIVE-CASES",
            testTitle = "Test MXE flow onboard and deploy commands negative cases")
    @Parameters({"flowName", "flowFile", "flowDeploymentName"})
    public void testMxeFlowOnboardAndDeployNegativeCases(String flowName, String flowFile, String flowDeploymentName) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            executeInStep(mxeCliDriver, () -> Commands.mxeFlow().onboard(flowFile, flowName),
                    "Onboard flow '" + flowName + "'");

            checkIfFlowExists(mxeCliDriver, flowName);

            onboardAlreadyExistingFlow(mxeCliDriver, flowName, flowFile);

            executeInStep(mxeCliDriver, () -> Commands.mxeFlow().deploy(flowDeploymentName, flowName),
                    "Deploy flow as '" + flowDeploymentName + "'");

            checkIfFlowDeploymentExists(mxeCliDriver, flowDeploymentName);

            deployAlreadyExistingFlow(mxeCliDriver, flowName, flowDeploymentName);

            executeInStep(mxeCliDriver, () -> Commands.mxeFlow().deleteDeployment(flowDeploymentName),
                    "Delete flow deployment '" + flowDeploymentName + "'");

            checkIfFlowDeploymentNotExists(mxeCliDriver, flowDeploymentName);

            deleteNotExistingDeployment(mxeCliDriver, flowDeploymentName);

            executeInStep(mxeCliDriver, () -> Commands.mxeFlow().deleteFlow(flowName),
                    "Delete flow '" + flowName + "'");

            checkIfFlowNotExists(mxeCliDriver, flowName);

            deleteNotExistingFlow(mxeCliDriver, flowName);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    private void onboardAlreadyExistingFlow(MxeCliDriver mxeCliDriver, String flowName, String flowFile) {
        setTestStepBegin("Onboard already existing flow '%s'", flowName);
        CustomCommand customCommand = Commands.mxeFlow().onboard(flowFile, flowName);
        assertFailedResult(mxeCliDriver, customCommand, ".*already exists.*");
        setTestStepEnd();
    }

    private void deployAlreadyExistingFlow(MxeCliDriver mxeCliDriver, String flowName, String flowDeploymentName) {
        setTestStepBegin("Deploy already existing flow '%s'", flowDeploymentName);
        CustomCommand customCommand = Commands.mxeFlow().deploy(flowDeploymentName, flowName);
        assertFailedResult(mxeCliDriver, customCommand, ".*already exists.*");
        setTestStepEnd();
    }

    private void deleteNotExistingFlow(MxeCliDriver mxeCliDriver, String flowName) {
        setTestStepBegin("Delete not existing flow '%s'", flowName);
        CustomCommand customCommand = Commands.mxeFlow().deleteFlow(flowName);
        assertFailedResult(mxeCliDriver, customCommand, ".*does not exist.*");
        setTestStepEnd();
    }

    private void deleteNotExistingDeployment(MxeCliDriver mxeCliDriver, String flowDeploymentName) {
        setTestStepBegin("Delete not existing deployment '%s'", flowDeploymentName);
        CustomCommand customCommand = Commands.mxeFlow().deleteDeployment(flowDeploymentName);
        assertFailedResult(mxeCliDriver, customCommand, ".*does not exist.*");
        setTestStepEnd();
    }

    private void assertFailedResult(MxeCliDriver mxeCliDriver, CustomCommand customCommand, String expectedOutputMatcher) {
        CommandResult result = mxeCliDriver.execute(customCommand);
        assertThat(result).isNotNull();
        saveAssertThat(result.getExitCode()).as(ERROR_EXIT_CODE_ZERO).isNotEqualTo(0);
        saveAssertThat(result.getCommandOutput()).matches(Pattern.compile(expectedOutputMatcher, Pattern.DOTALL));
    }

    private void checkIfFlowDeploymentExists(MxeCliDriver mxeCliDriver, String flowDeploymentName) {
        setTestStepBegin("Check if flow deployment '%s' exists", flowDeploymentName);
        MxeFlowCommand listFlowDeploymentsCmd = Commands.mxeFlow().listDeployments();
        Optional<CommandResult> resultOpt = executeCommand(mxeCliDriver, listFlowDeploymentsCmd);
        resultOpt.map(MxeTestBase::parseListCommandResult).ifPresent(l -> saveAssertThat(l)
                .anyMatch(m -> m.getOrDefault(HEADER_ELEMENT_NAME, StringUtils.EMPTY).equals(flowDeploymentName)));
        setTestStepEnd();
    }

    private void checkIfFlowDeploymentNotExists(MxeCliDriver mxeCliDriver, String flowDeploymentName) {
        setTestStepBegin("Check if flow deployment '%s' NOT exists", flowDeploymentName);
        MxeFlowCommand listFlowDeploymentsCmd = Commands.mxeFlow().listDeployments();
        Optional<CommandResult> resultOpt = executeCommand(mxeCliDriver, listFlowDeploymentsCmd);
        resultOpt.map(MxeTestBase::parseListCommandResult).ifPresent(l -> saveAssertThat(l)
                .noneMatch(m -> m.getOrDefault(HEADER_ELEMENT_NAME, StringUtils.EMPTY).equals(flowDeploymentName)));
        setTestStepEnd();
    }

    private void checkIfFlowExists(MxeCliDriver mxeCliDriver, String flowName) {
        setTestStepBegin("Check if flow '%s' exists", flowName);

        MxeFlowCommand listFlowsCmd = Commands.mxeFlow().listFlows();
        Optional<CommandResult> resultOpt = executeCommand(mxeCliDriver, listFlowsCmd);

        resultOpt.map(r -> String.valueOf(r.getCommandOutput()))
                .ifPresent(r -> saveAssertThat(r.split("\n")).contains(flowName));
        setTestStepEnd();
    }

    private void checkIfFlowNotExists(MxeCliDriver mxeCliDriver, String flowName) {
        setTestStepBegin("Check if flow '%s' NOT exists", flowName);

        MxeFlowCommand listFlowsCmd = Commands.mxeFlow().listFlows();
        Optional<CommandResult> resultOpt = executeCommand(mxeCliDriver, listFlowsCmd);

        resultOpt.map(r -> String.valueOf(r.getCommandOutput()))
                .ifPresent(r -> saveAssertThat(r.split("\n")).doesNotContain(flowName));
        setTestStepEnd();
    }
}
