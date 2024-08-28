package com.ericsson.mxe.jcat.test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ericsson.mxe.jcat.command.Commands;
import com.ericsson.mxe.jcat.command.CustomCommand;
import com.ericsson.mxe.jcat.command.result.CommandResult;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;

import se.ericsson.jcat.fw.annotations.JcatClass;
import se.ericsson.jcat.fw.annotations.JcatMethod;
import se.ericsson.jcat.fw.annotations.Teardown;
import se.ericsson.jcat.fw.model.JcatModelHolder;

/**
 * @JcatDocChapterDescription Chapter covering model function tests.
 */
@JcatClass(chapterName = "Model Function Tests")
public class MxeModelTest extends MxeTestBase {

    private static final Duration MODEL_START_TIMEOUT = Duration.ofMinutes(5);
    private static final String STATUS_RUNNING = "running";
    private static final String ERROR_FAILED_TO_EXECUTE = "Failed to execute command, commandResult is null";
    private static final String ERROR_EXIT_CODE_NOT_ZERO = "CustomCommand exit code was not 0";
    private static final String ERROR_OUTPUT_NOT_FOUND = "Expected output not found";
    private static final String HEADER_ELEMENT_VERSION = "VERSION";
    private static final String HEADER_ELEMENT_STATUS = "STATUS";
    private static final String HEADER_ELEMENT_INSTANCES = "INSTANCES";
    private static final String HEADER_ELEMENT_IMAGE_A = "IMAGE_A";

    /**
     * @JcatTcDescription List MXE model commands
     * @JcatTcPreconditions None
     * @JcatTcInstruction The testcase is about running the mxe-model CLI to print list related help messages using default cluster
     * @JcatTcAction mxe-model list --help --verbose
     * @JcatTcActionResult Help message is printed
     * @JcatTcAction mxe-model list --verbose
     * @JcatTcActionResult Help message is printed
     * @JcatTcAction mxe-model list --help
     * @JcatTcActionResult Help message is printed
     * @JcatTcAction mxe-model list
     * @JcatTcActionResult Help message is printed
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-MODEL-COMMANDS", testTitle = "List MXE model commands")
    public void testMxeModelPrintHelp() {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            executeInStep(mxeCliDriver, () -> Commands.mxeModel().list().help().verbose(),
                    "Model list help verbose");
            executeInStep(mxeCliDriver, () -> Commands.mxeModel().list().verbose(), "Model list verbose");
            executeInStep(mxeCliDriver, () -> Commands.mxeModel().list().help(), "Model list help");
            executeInStep(mxeCliDriver, () -> Commands.mxeModel().list(), "Model list");
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Start and stop a model on local cluster
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about onboarding and starting an mxe model
     * @JcatTcAction Onboard the model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded model appears in the model list
     * @JcatTcAction Start the onboarded model
     * @JcatTcActionResult Model started successfully
     * @JcatTcAction List started models
     * @JcatTcActionResult Previously started model appears in the model list
     * @JcatTcAction Check if the model is started with the given instance number
     * @JcatTcActionResult Model is started with the given instance number
     * @JcatTcAction Using the list started models wait until the the model reaches "RUNNING" status (timeout is 5 minutes)
     * @JcatTcActionResult Model reaches "RUNNING" status
     * @JcatTcAction Stop started model
     * @JcatTcActionResult Model stopped successfully
     * @JcatTcAction Delete model
     * @JcatTcActionResult Model deleted successfully
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({ "packageName", "modelName", "modelVersion", "instanceName", "instance" })
    @JcatMethod(testTag = "TEST-MXE-MODEL-START", testTitle = "Start and stop a model on local cluster")
    public void testMxeModelStart(String packageName, String modelName, String modelVersion, @org.testng.annotations.Optional String instanceName, int instance) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            mxeModelOnboard(mxeCliDriver, modelName, modelVersion, packageName);
            mxeModelListOnboarded(mxeCliDriver);
            instanceName = mxeModelStart(mxeCliDriver, instanceName, modelName, modelVersion, instance);
            mxeModelListStarted(mxeCliDriver);
            verifyModelInstanceNumber(mxeCliDriver, instanceName, instance);
            waitUntilModelStatusIs(mxeCliDriver, instanceName, STATUS_RUNNING, MODEL_START_TIMEOUT);
            mxeModelStop(mxeCliDriver, instanceName);
            mxeModelDelete(mxeCliDriver, modelName, modelVersion);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Start and stop a model on local cluster: negative cases
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about testing the negative cases of onboard, start, stop and delete an mxe model
     * @JcatTcAction Onboard the model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded model appears in the model list
     * @JcatTcAction Start the onboarded model
     * @JcatTcActionResult Model started successfully
     * @JcatTcAction List started models
     * @JcatTcActionResult Previously started model appears in the model list
     * @JcatTcAction Check if the model is started with the given instance number
     * @JcatTcActionResult Model is started with the given instance number
     * @JcatTcAction Start the already started model again
     * @JcatTcActionResult Model start should fail
     * @JcatTcAction Stop started model
     * @JcatTcActionResult Model stopped successfully
     * @JcatTcAction Stop a model with non-existing name
     * @JcatTcActionResult Model stop should fail
     * @JcatTcAction Delete model
     * @JcatTcActionResult Model deleted successfully
     * @JcatTcAction Delete a non-existing model
     * @JcatTcActionResult Model deleted should fail
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({ "packageName", "modelName", "modelVersion", "instance" })
    @JcatMethod(testTag = "TEST-MXE-MODEL-START-NEGATIVE-CASES", testTitle = "Start and stop a model on local cluster: negative cases")
    public void testMxeModelStartNegativeCases(String packageName, String modelName, String modelVersion, int instance) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            mxeModelOnboard(mxeCliDriver, modelName, modelVersion, packageName);
            mxeModelListOnboarded(mxeCliDriver);
            String instanceName = mxeModelStart(mxeCliDriver, null, modelName, modelVersion, instance);
            mxeModelListStarted(mxeCliDriver);
            verifyModelInstanceNumber(mxeCliDriver, instanceName, instance);
            mxeModelStartAlreadyRunningExpectFail(mxeCliDriver, modelName, modelVersion, instance);
            mxeModelStop(mxeCliDriver, instanceName);
            mxeModelStopNonExistingModelExpectFail(mxeCliDriver);
            mxeModelDelete(mxeCliDriver, modelName, modelVersion);
            mxeModelDeleteNonExistingModelExpectFail(mxeCliDriver);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Scaling a model deployment
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about onboarding, starting and scaling an mxe model
     * @JcatTcAction Onboard the model
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded model appears in the model list
     * @JcatTcAction Start the onboarded model with initial instance number
     * @JcatTcActionResult Model started successfully
     * @JcatTcAction List started models
     * @JcatTcActionResult Previously started model appears in the model list
     * @JcatTcAction Check if the model is started with the initial instance number
     * @JcatTcActionResult Model is started with the initial instance number
     * @JcatTcAction Scale out model
     * @JcatTcActionResult Model scaled out successfully
     * @JcatTcAction Check if the model is running with the scaled-out instance number
     * @JcatTcActionResult Model is running with the scaled-out instance number
     * @JcatTcAction Scale in model to the initial instance number
     * @JcatTcActionResult Model scaled in successfully
     * @JcatTcAction Check if the model is running with the initial instance number
     * @JcatTcActionResult Model is running with the initial instance number
     * @JcatTcAction Scale a non-existing model
     * @JcatTcActionResult Model scaling should fail
     * @JcatTcAction Scale in model to a negative instance number
     * @JcatTcActionResult Model scaling should fail
     * @JcatTcAction Check if the model is running with the initial instance number
     * @JcatTcActionResult Model is running with the initial instance number
     * @JcatTcAction Stop started model
     * @JcatTcActionResult Model stopped successfully
     * @JcatTcAction Delete model
     * @JcatTcActionResult Model deleted successfully
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({ "packageName", "modelName", "modelVersion", "instanceName" })
    @JcatMethod(testTag = "TEST-MXE-MODEL-SCALE", testTitle = "Scaling a model deployment")
    public void testMxeModelScale(String packageName, String modelName, String modelVersion, @org.testng.annotations.Optional String instanceName) {
        final int initialInstanceNumber = 1;
        final int scaleInstanceNumber = 2;
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            // Onboard a model and start a test model deployment
            mxeModelOnboard(mxeCliDriver, modelName, modelVersion, packageName);
            mxeModelListOnboarded(mxeCliDriver);
            instanceName = mxeModelStart(mxeCliDriver, instanceName, modelName, modelVersion, initialInstanceNumber);
            verifyModelInstanceNumber(mxeCliDriver, instanceName, initialInstanceNumber);

            // Testing scale to the same instance number
            mxeModelScale(mxeCliDriver, instanceName, initialInstanceNumber, initialInstanceNumber);
            verifyModelInstanceNumber(mxeCliDriver, instanceName, initialInstanceNumber);

            // Testing scale-out
            mxeModelScale(mxeCliDriver, instanceName, initialInstanceNumber, scaleInstanceNumber);
            verifyModelInstanceNumber(mxeCliDriver, instanceName, scaleInstanceNumber);

            // Testing scale-in
            mxeModelScale(mxeCliDriver, instanceName, scaleInstanceNumber, initialInstanceNumber);
            verifyModelInstanceNumber(mxeCliDriver, instanceName, initialInstanceNumber);

            // Negative test of scaling, unknown model
            mxeModelScaleFailed(mxeCliDriver);

            // Testcase cleanup
            mxeModelStop(mxeCliDriver, instanceName);
            mxeModelDelete(mxeCliDriver, modelName, modelVersion);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    /**
     * @JcatTcDescription Upgrading a model deployment
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about upgrading an mxe model
     * @JcatTcAction Onboard the model with the initial version
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded model appears in the model list
     * @JcatTcAction Onboard the model with the target version
     * @JcatTcActionResult Model is onboarded successfully
     * @JcatTcAction List onboarded models
     * @JcatTcActionResult Previously onboarded model appears in the model list
     * @JcatTcAction Start the onboarded model with initial version and instance number
     * @JcatTcActionResult Model started successfully
     * @JcatTcAction Check if the model is started with the initial version
     * @JcatTcActionResult Previously started model is running with the initial version
     * @JcatTcAction Upgrade an unknown model
     * @JcatTcActionResult Model upgrade should fail
     * @JcatTcAction Upgrade the model to the current version
     * @JcatTcActionResult Model upgrade should fail
     * @JcatTcAction Upgrade the model to an unknown version
     * @JcatTcActionResult Model upgrade should fail
     * @JcatTcAction Downgrade the model to a lesser version
     * @JcatTcActionResult Model upgrade should fail
     * @JcatTcAction Check if the model is running with the initial version
     * @JcatTcActionResult Model is running with the initial version
     * @JcatTcAction Upgrade the model to the target version
     * @JcatTcActionResult Model upgraded successfully
     * @JcatTcAction Check if the model is running with the initial version
     * @JcatTcActionResult Model is running with the target version
     * @JcatTcAction Stop started model
     * @JcatTcActionResult Model stopped successfully
     * @JcatTcAction Delete model with the initial version
     * @JcatTcActionResult Model deleted successfully
     * @JcatTcAction Delete model with the target version
     * @JcatTcActionResult Model deleted successfully
     * @JcatTcPostconditions NA
     */
    @Test
    @Parameters({ "packageName", "targetPackageName", "modelName", "modelVersion", "targetModelVersion", "unknownModelVersion", "instanceName", "instance" })
    @JcatMethod(testTag = "TEST-MXE-MODEL-UPGRADE", testTitle = "Upgrading a model deployment")
    @SuppressWarnings("squid:S00107")
    public void testMxeModelUpgrade(String packageName, String targetPackageName, String modelName, String modelVersion, String targetModelVersion,
            String unknownModelVersion, @org.testng.annotations.Optional String instanceName, int instance) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            // Onboard models and start a test model deployment
            mxeModelOnboard(mxeCliDriver, modelName, modelVersion, packageName);
            mxeModelListOnboarded(mxeCliDriver);
            mxeModelOnboard(mxeCliDriver, modelName, targetModelVersion, targetPackageName);
            mxeModelListOnboarded(mxeCliDriver);
            instanceName = mxeModelStart(mxeCliDriver, instanceName, modelName, modelVersion, instance);
            verifyModelDeploymentVersion(mxeCliDriver, instanceName, modelVersion);

            // Negative test of model deployment upgrade
            mxeModelUpgradeFailed(mxeCliDriver, instanceName, modelVersion, unknownModelVersion);
            verifyModelDeploymentVersion(mxeCliDriver, instanceName, modelVersion);

            // Model deployment upgrade test
            mxeModelUpgrade(mxeCliDriver, instanceName, targetModelVersion);
            verifyModelDeploymentVersion(mxeCliDriver, instanceName, targetModelVersion);

            // Testcase cleanup, stop model deployments and delete onboarded models
            mxeModelStop(mxeCliDriver, instanceName);
            mxeModelDelete(mxeCliDriver, modelName, modelVersion);
            mxeModelDelete(mxeCliDriver, modelName, targetModelVersion);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    @Teardown(alwaysRun = true)
    public void cleanup() {
        switch (JcatModelHolder.getCurrentTestCase().getTestResult()) {
        case ERROR:
        case FAILED:
        case INCONCLUSIVE: {
            setTestStepBegin("Cleanup");
            stopAllModelsInSubstep();
            deleteAllModelsInSubstep();
            setTestStepEnd();
        }
        default: {
            // NOOP
        }
        }
    }

    private void deleteAllModelsInSubstep() {
        setSubTestStep("Delete models");
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            CustomCommand customCommand = Commands.mxeModel().listOnboarded();
            CommandResult commandResult = mxeCliDriver.execute(customCommand);
            assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
            List<Map<String, String>> result = parseListCommandResult(commandResult);
            setTestInfoExpandable("Deployed models:", String.valueOf(result));
            result.forEach(map -> mxeCliDriver.execute(
                    Commands.mxeModel().delete(map.get(HEADER_ELEMENT_NAME), map.get(HEADER_ELEMENT_VERSION))));
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
        setSubTestStepEnd();
    }

    private void stopAllModelsInSubstep() {
        setSubTestStep("Stop models");
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            List<Map<String, String>> startedModelList = getStartedModelList(mxeCliDriver);
            setTestInfoExpandable("Started models:", String.valueOf(startedModelList));
            startedModelList.forEach(map -> {
                mxeCliDriver.execute(Commands.mxeModel().stop(map.get(HEADER_ELEMENT_NAME)));
            });

        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
        setSubTestStepEnd();
    }

    private void waitUntilModelStatusIs(MxeCliDriver mxeCliDriver, String instanceName, String expectedStatus,
            Duration timeOut) {
        setTestStepBegin("Check if " + instanceName + " status is '" + expectedStatus + "' (timeout " + timeOut + ")");
        final long endTime = System.currentTimeMillis() + timeOut.toMillis();
        final int sleepSec = 10;

        int counter = 1;
        do {
            setSubTestStep("Check counter:" + counter++);
            setTestInfo("Sleeping " + sleepSec + "sec");
            try {
                TimeUnit.SECONDS.sleep(sleepSec);
            } catch (InterruptedException e) {
                setTestWarning("Sleep interrupted");
                Thread.currentThread().interrupt();
            }
            Optional<Map<String, String>> first = getStartedModelList(mxeCliDriver).stream()
                    .filter(m -> StringUtils.equals(m.get(HEADER_ELEMENT_NAME), instanceName)).findFirst();
            if (!first.isPresent()) {
                saveFail("There is no " + instanceName);
                return;
            }

            Map<String, String> modelInfo = first.get();
            if (StringUtils.equals(modelInfo.get(HEADER_ELEMENT_STATUS), expectedStatus)) {
                setTestInfo("Status of " + instanceName + " is '" + expectedStatus + "'");
                return;
            }
            setTestInfo("Status of %s is '%s', waiting some more (%s left)", instanceName,
                    modelInfo.get(HEADER_ELEMENT_STATUS), Duration.ofMillis(endTime - System.currentTimeMillis()));

        } while (System.currentTimeMillis() < endTime);
        saveFail("Status of " + instanceName + " have not reached '" + expectedStatus + "' until " + timeOut);
    }

    private void mxeModelOnboard(final MxeCliDriver mxeCliDriver, String modelName, String modelVersion, String packageName) {
        setTestStepBegin("Onboard model '" + modelName + "'");
        CustomCommand customCommand = Commands.mxeModel().onboard(modelName, modelVersion, packageName);
        CommandResult commandResult = mxeCliDriver.execute(customCommand);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertEquals(ERROR_EXIT_CODE_NOT_ZERO, 0, commandResult.getExitCode());
        String successPattern = "Success: Model service package \"" + packageName + "\" has been onboarded to cluster \".*\" with name \"" + modelName
                + "\" and version \"" + modelVersion + "\".*";
        saveAssertTrue(ERROR_OUTPUT_NOT_FOUND, commandResult.getCommandOutput().matches(successPattern));
        setTestStepEnd();
    }

    private void mxeModelDelete(final MxeCliDriver mxeCliDriver, String modelName, String modelVersion) {
        setTestStepBegin("Delete model " + modelName + " with version " + modelVersion);
        CustomCommand customCommand = Commands.mxeModel().delete(modelName, modelVersion);
        CommandResult commandResult = mxeCliDriver.execute(customCommand);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertEquals(ERROR_EXIT_CODE_NOT_ZERO, 0, commandResult.getExitCode());
        String successPattern = "Success: Model service package \"" + modelName + "\" has been deleted.*";
        saveAssertTrue(ERROR_OUTPUT_NOT_FOUND, commandResult.getCommandOutput().matches(successPattern));
        setTestStepEnd();
    }

    private void mxeModelListOnboarded(final MxeCliDriver mxeCliDriver) {
        setTestStepBegin("List onboarded models");
        CustomCommand customCommand = Commands.mxeModel().listOnboarded();
        CommandResult commandResult = mxeCliDriver.execute(customCommand);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertEquals(ERROR_EXIT_CODE_NOT_ZERO, 0, commandResult.getExitCode());
        setTestStepEnd();
    }

    private String mxeModelStart(final MxeCliDriver mxeCliDriver, String instanceName, String modelName, String modelVersion, int instance) {
        setTestStepBegin("Start model " + modelName + " with version " + modelVersion + " with instance number " + instance);
        final CustomCommand customCommand;
        if (instanceName == null) {
            customCommand = Commands.mxeModel().start(modelName + ":" + modelVersion, instance);
        } else {
            customCommand = Commands.mxeModel().start(instanceName, modelName + ":" + modelVersion, instance);
        }
        CommandResult commandResult = mxeCliDriver.execute(customCommand);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertEquals(ERROR_EXIT_CODE_NOT_ZERO, 0, commandResult.getExitCode());
        String successPattern = "Success: Model service has been started in \".*\" with name \"([a-zA-Z\\-0-9]+)\""
                + ((instance > 1) ? " in " + instance + " instances." : ".");
        final Matcher matcher = Pattern.compile(successPattern).matcher(commandResult.getCommandOutput());
        assertThat(commandResult.getCommandOutput()).as(ERROR_OUTPUT_NOT_FOUND).matches(successPattern);
        String returnedInstanceName = null;
        if (matcher.matches()) {
            returnedInstanceName = matcher.group(1);
            if (instanceName != null) {
                saveAssertEquals("Returned instance name differs from the expected", instanceName, returnedInstanceName);
            }
        }
        setTestStepEnd();
        return returnedInstanceName;
    }

    private void mxeModelStartAlreadyRunningExpectFail(final MxeCliDriver mxeCliDriver, String modelName, String modelVersion, int instance) {
        setTestStepBegin("Start mxe model " + modelName + " with version " + modelVersion + " with instance number " + instance + " which is already running");
        final CustomCommand customCommand;
        customCommand = Commands.mxeModel().start(modelName + ":" + modelVersion, instance);
        CommandResult commandResult = mxeCliDriver.execute(customCommand);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertNotEquals(ERROR_EXIT_CODE_ZERO, 0, commandResult.getExitCode());
        saveAssertTrue(ERROR_OUTPUT_NOT_FOUND, commandResult.getCommandOutput().matches("Error: MXE model deployment already exists"));
        setTestStepEnd();
    }

    private void mxeModelStopNonExistingModelExpectFail(final MxeCliDriver mxeCliDriver) {
        setTestStepBegin("Stop non-existing model");
        CustomCommand customCommand = Commands.mxeModel().stop("nonexistingname");
        CommandResult commandResult = mxeCliDriver.execute(customCommand);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertNotEquals(ERROR_EXIT_CODE_ZERO, 0, commandResult.getExitCode());
        saveAssertTrue(ERROR_OUTPUT_NOT_FOUND, commandResult.getCommandOutput().matches("Error: MXE model deployment not found"));
        setTestStepEnd();
    }

    private void mxeModelDeleteNonExistingModelExpectFail(final MxeCliDriver mxeCliDriver) {
        setTestStepBegin("Delete non-existing model");
        CustomCommand customCommand = Commands.mxeModel().delete("nonexistingname", "1.2.3");
        CommandResult commandResult = mxeCliDriver.execute(customCommand);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertNotEquals(ERROR_EXIT_CODE_ZERO, 0, commandResult.getExitCode());
        saveAssertTrue(ERROR_OUTPUT_NOT_FOUND, commandResult.getCommandOutput().matches("Error: MXE resource not found"));
        setTestStepEnd();
    }

    private void mxeModelScale(final MxeCliDriver mxeCliDriver, String instanceName, int oldInstance, int newInstance) {
        setTestStepBegin("Scaling mxe model deployment " + instanceName + " from instance number " + oldInstance + " to " + newInstance);
        CustomCommand customCommand = Commands.mxeModel().scale(instanceName, newInstance);
        CommandResult commandResult = mxeCliDriver.execute(customCommand);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertEquals(ERROR_EXIT_CODE_NOT_ZERO, 0, commandResult.getExitCode());
        if (oldInstance != newInstance) {
            String successPattern = "Success: Number of instances of modeldeployment " + instanceName + " is set to " + newInstance;
            saveAssertTrue(ERROR_OUTPUT_NOT_FOUND, commandResult.getCommandOutput().matches(successPattern));
        } else {
            String successPattern = "Success: Nothing to do, modeldeployment \\[" + instanceName + "\\] has " + newInstance + " instances already.";
            saveAssertTrue(ERROR_OUTPUT_NOT_FOUND, commandResult.getCommandOutput().matches(successPattern));
        }
        setTestStepEnd();
    }

    private void mxeModelScaleFailed(final MxeCliDriver mxeCliDriver) {
        setTestStepBegin("Scaling test of an unknown model deployment");
        String notExistingModelName = "unknown";
        CustomCommand customCommand = Commands.mxeModel().scale(notExistingModelName, 1);
        CommandResult commandResult = mxeCliDriver.execute(customCommand);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertNotEquals(ERROR_EXIT_CODE_ZERO, 0, commandResult.getExitCode());
        String failurePattern = "Error: Model identified by \"" + notExistingModelName + "\" does not exist";
        saveAssertTrue(ERROR_OUTPUT_NOT_FOUND, commandResult.getCommandOutput().matches(failurePattern));
        setTestStepEnd();
    }

    private void mxeModelUpgrade(final MxeCliDriver mxeCliDriver, String deploymentName, String targetVersion) {
        setTestStepBegin("Upgrading mxe model deployment " + deploymentName + " to version " + targetVersion);
        CustomCommand customCommand = Commands.mxeModel().upgrade(deploymentName, targetVersion);
        CommandResult commandResult = mxeCliDriver.execute(customCommand);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertEquals(ERROR_EXIT_CODE_NOT_ZERO, 0, commandResult.getExitCode());
        String successPattern = "Success: Version of modeldeployment " + deploymentName + " is set to " + targetVersion;
        saveAssertTrue(ERROR_OUTPUT_NOT_FOUND, commandResult.getCommandOutput().matches(successPattern));
        setTestStepEnd();
    }

    private void mxeModelUpgradeFailed(final MxeCliDriver mxeCliDriver, String deploymentName, String currentVersion, String unknownVersion) {
        setTestStepBegin("Mxe model deployment upgrade failure test");
        mxeModelUpgradeFailedSubStep(mxeCliDriver, "Upgrade of an unknown model deployment", "unknown", unknownVersion,
                "Error: Model identified by \"unknown\" does not exist");
        mxeModelUpgradeFailedSubStep(mxeCliDriver, "Upgrade model deployment " + deploymentName + " to current version " + currentVersion, deploymentName,
                currentVersion, "Error: Model .* identified by \"" + deploymentName + "\" already on version \"" + currentVersion + "\"");
        mxeModelUpgradeFailedSubStep(mxeCliDriver, "Upgrade model deployment " + deploymentName + " to unknown version " + unknownVersion, deploymentName,
                unknownVersion, "Error: Model \".*\" identified by \"" + deploymentName + "\" does not have version \"" + unknownVersion + "\"");
        mxeModelUpgradeFailedSubStep(mxeCliDriver, "Downgrading model deployment " + deploymentName + " to version 0.0.0", deploymentName, "0.0.0",
                "Error: Model \".*\" identified by \"" + deploymentName + "\" cannot be upgraded to version \"0.0.0\", because it has a newer version \""
                        + currentVersion + "\" running on cluster .*");
        setTestStepEnd();
    }

    private void mxeModelUpgradeFailedSubStep(final MxeCliDriver mxeCliDriver, String testStepName, String deploymentName, String targetVersion,
            String errorPattern) {
        setSubTestStep(testStepName);
        CustomCommand customCommand = Commands.mxeModel().upgrade(deploymentName, targetVersion);
        CommandResult commandResult = mxeCliDriver.execute(customCommand);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertNotEquals("Command exit code was 0", 0, commandResult.getExitCode());
        saveAssertTrue(ERROR_OUTPUT_NOT_FOUND, commandResult.getCommandOutput().matches(errorPattern));
    }

    private void mxeModelListStarted(final MxeCliDriver mxeCliDriver) {
        setTestStepBegin("List started models");
        getStartedModelList(mxeCliDriver);
        setTestStepEnd();
    }

    private void verifyModelInstanceNumber(final MxeCliDriver mxeCliDriver, String modelDeploymentName, int instance) {
        setTestStepBegin("Check instance number");
        Map<String, String> model = getModelFromList(getStartedModelList(mxeCliDriver), HEADER_ELEMENT_NAME, modelDeploymentName);
        saveAssertTrue("Instance number of model deployment " + modelDeploymentName + " is not " + instance,
                model != null && model.get(HEADER_ELEMENT_INSTANCES) != null && Integer.parseInt(model.get(HEADER_ELEMENT_INSTANCES)) == instance);
        setTestStepEnd();
    }

    private void verifyModelDeploymentVersion(final MxeCliDriver mxeCliDriver, String modelDeploymentName, String version) {
        setTestStepBegin("Check model deployment version");
        Map<String, String> model = getModelFromList(getStartedModelList(mxeCliDriver), HEADER_ELEMENT_NAME, modelDeploymentName);
        saveAssertTrue("Version of model deployment " + modelDeploymentName + " is not " + version, model != null && model.get(HEADER_ELEMENT_IMAGE_A) != null
                && model.get(HEADER_ELEMENT_IMAGE_A).endsWith(version));
        setTestStepEnd();
    }

    private List<Map<String, String>> getStartedModelList(final MxeCliDriver mxeCliDriver) {
        CustomCommand customCommand = Commands.mxeModel().listStarted();
        CommandResult commandResult = mxeCliDriver.execute(customCommand);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertEquals(ERROR_EXIT_CODE_NOT_ZERO, 0, commandResult.getExitCode());
        return parseListCommandResult(commandResult);
    }

    private Map<String, String> getModelFromList(List<Map<String, String>> modelList, String filterKey, String filterValue) {
        for (Map<String, String> model : modelList) {
            if (model.get(filterKey) != null && model.get(filterKey).equals(filterValue)) {
                return model;
            }
        }
        return null;
    }

    private void mxeModelStop(final MxeCliDriver mxeCliDriver, String instanceName) {
        setTestStepBegin("Stop model '" + instanceName + "'");
        CustomCommand customCommand = Commands.mxeModel().stop(instanceName);
        CommandResult commandResult = mxeCliDriver.execute(customCommand);
        assertNotNull(ERROR_FAILED_TO_EXECUTE, commandResult);
        saveAssertEquals(ERROR_EXIT_CODE_NOT_ZERO, 0, commandResult.getExitCode());
        String successPattern = "Success: " + instanceName + " has been stopped.";
        saveAssertTrue(ERROR_OUTPUT_NOT_FOUND, commandResult.getCommandOutput().matches(successPattern));
        setTestStepEnd();
    }
}
