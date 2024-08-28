package com.ericsson.mxe.jcat.test;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.JcatClass;
import se.ericsson.jcat.fw.annotations.JcatMethod;

/**
 * @JcatDocChapterDescription Chapter covering scalability tests.
 */
@JcatClass(chapterName = "Scalability Tests")
public class MxeScalabilityTest extends MxeTestBase {

    /**
     * @JcatTcDescription Performs performance test with a model deployment
     * @JcatTcPreconditions MXE cluster is up and running in a separated characteristics environment
     * @JcatTcInstruction The testcase is about sending data to a running model deployment with fixed rate and changing model instance number and measure the
     *                    following characteristics for each send rate: <br/>
     *                    <b>Model level measurements provided by prometheus</b>
     *                    <ul>
     *                    <li><u>request rate:</u> Average request rate of the model deployment during the measurement</li>
     *                    <li><u>latency:</u> Average latency of the model deployment during the measurement</li>
     *                    <li><u>success rate:</u> Average success rate of the model deployment during the measurement</li>
     *                    <li><u>400 response op/sec:</u> Average 400 response op/sec of the model deployment during the measurement</li>
     *                    <li><u>500 response op/sec:</u> Average 500 response op/sec of the model deployment during the measurement</li>
     *                    </ul>
     *                    <b>Cluster level measurements provided by prometheus</b>
     *                    <ul>
     *                    <li><u>memory usage:</u> Memory usage of the cluster</li>
     *                    <li><u>CPU:</u> CPU load of the cluster</li>
     *                    <li><u>Filesystem usage:</u> Filesystem usage load of the cluster</li>
     *                    <li><u>Network usage:</u> Network usage load of the cluster</li>
     *                    </ul>
     *                    <b>Pod level measurements provided by prometheus</b>
     *                    <ul>
     *                    <li><u>memory usage:</u> Memory usage of the pods</li>
     *                    <li><u>CPU:</u> CPU load of the pods</li>
     *                    <li><u>Filesystem usage:</u> Filesystem usage load of the pods</li>
     *                    <li><u>Network usage:</u> Network usage load of the pods</li>
     *                    </ul>
     *                    Since several preparatory steps are needed for execution of this scalability test case, the followings are needed:
     *                    <ul>
     *                    <li>always execute with a predefined test suite</li>
     *                    </ul>
     *                    <b>Scalability test parameters</b>
     *                    <ul>
     *                    <li><u>packageName:</u> Model package nam and path to be used during the test</li>
     *                    <li><u>modelName:</u> Name of the model after onboarding</li>
     *                    <li><u>modelVersion:</u>Version of the model</li>
     *                    <li><u>instanceName:</u>Name of the model deployment instance</li>
     *                    <li><u>instance:</u>Model deployment instance number</li>
     *                    <li><u>maxInstance:</u>Maximum instance number</li>
     *                    <li><u>modelInputJsonPath:</u>Input json for model prediction</li>
     *                    <li><u>sendRate:</u>Send rate we would like to reach during the measurement</li>
     *                    <li><u>measurementLength:</u>Length of a measurement step in seconds</li>
     *                    </ul>
     * @JcatTcAction Onboard model
     * @JcatTcActionResult Model package onboarded and available in MXE
     * @JcatTcAction Start a model deploymentent is running
     * @JcatTcActionResult Model is running
     * @JcatTcAction Read model input json file
     * @JcatTcActionResult Model input lines are collected, which will be used for prediction
     * @JcatTcAction Start sending traffic with the selected send rate
     * @JcatTcActionResult Traffic sending started
     * @JcatTcAction Continously collect all the previously listed measurements from prometheus and store it in CSV file
     * @JcatTcActionResult Data got from prometheus and stored in CSV file
     * @JcatTcAction Stop traffic after measurementLength expired
     * @JcatTcActionResult Traffic stopped
     * @JcatTcAction Scale model deployment, increase instance with one
     * @JcatTcActionResult Model scaled out
     * @JcatTcAction Perform the previous model scaling, traffic starting and measurement steps for all of the instance number till we reached maxInstance with
     *               fix send rate
     * @JcatTcActionResult Measurement repeated for all of the instance number till we reached maxInstance
     * @JcatTcAction Stop data collection after measurementLength expired
     * @JcatTcActionResult Data collection stopped
     * @JcatTcAction Draw diagrams about the collected data. Separate diagram for all measurement type which contains measured values for each send rate
     * @JcatTcActionResult Diagrams generated
     * @JcatTcAction Stop previously started model deployment
     * @JcatTcActionResult Model deployment stopped
     * @JcatTcAction Delete onboarded model
     * @JcatTcActionResult Model deleted
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-MODEL-SCALABILITY", testTitle = "MXE model scalability test")
    @Parameters({ "packageName", "modelName", "modelVersion", "instanceName", "instance", "maxInstance", "modelInputJsonPath", "maxRate", "measurementLength" })
    public void testMxeModelScalabity(String packageName, String modelName, String modelVersion, String instanceName, int instance, int maxInstance,
            String modelInputJsonPath, int sendRate, int measurementLength) {
        // Not implemented yet
    }

    /**
     * @JcatTcDescription Performs performance test with a model deployment in a flow
     * @JcatTcPreconditions MXE cluster is up and running in a separated characteristics environment
     * @JcatTcInstruction The testcase is about sending data to a running model deployment in a flow with fixed rate and changing model instance number and
     *                    measure the following characteristics for each send rate: <br/>
     *                    <b>Model level measurements provided by prometheus</b>
     *                    <ul>
     *                    <li><u>request rate:</u> Average request rate of the model deployment during the measurement</li>
     *                    <li><u>latency:</u> Average latency of the model deployment during the measurement</li>
     *                    <li><u>success rate:</u> Average success rate of the model deployment during the measurement</li>
     *                    <li><u>400 response op/sec:</u> Average 400 response op/sec of the model deployment during the measurement</li>
     *                    <li><u>500 response op/sec:</u> Average 500 response op/sec of the model deployment during the measurement</li>
     *                    </ul>
     *                    <b>Cluster level measurements provided by prometheus</b>
     *                    <ul>
     *                    <li><u>memory usage:</u> Memory usage of the cluster</li>
     *                    <li><u>CPU:</u> CPU load of the cluster</li>
     *                    <li><u>Filesystem usage:</u> Filesystem usage load of the cluster</li>
     *                    <li><u>Network usage:</u> Network usage load of the cluster</li>
     *                    </ul>
     *                    <b>Pod level measurements provided by prometheus</b>
     *                    <ul>
     *                    <li><u>memory usage:</u> Memory usage of the pods</li>
     *                    <li><u>CPU:</u> CPU load of the pods</li>
     *                    <li><u>Filesystem usage:</u> Filesystem usage load of the pods</li>
     *                    <li><u>Network usage:</u> Network usage load of the pods</li>
     *                    </ul>
     *                    Since several preparatory steps are needed for execution of this scalability test case, the followings are needed:
     *                    <ul>
     *                    <li>always execute with a predefined test suite</li>
     *                    </ul>
     *                    <b>Scalability test parameters</b>
     *                    <ul>
     *                    <li><u>packageName:</u> Model package nam and path to be used during the test</li>
     *                    <li><u>modelName:</u> Name of the model after onboarding</li>
     *                    <li><u>modelVersion:</u>Version of the model</li>
     *                    <li><u>instanceName:</u>Name of the model deployment instance</li>
     *                    <li><u>instance:</u>Model deployment instance number</li>
     *                    <li><u>maxInstance:</u>Maximum instance number</li>
     *                    <li><u>flowName:</u>Name of the flow to onboard and deploy</li>
     *                    <li><u>modelInputJsonPath:</u>Input json for model prediction</li>
     *                    <li><u>sendRate:</u>Send rate we would like to reach during the measurement</li>
     *                    <li><u>measurementLength:</u>Length of a measurement step in seconds</li>
     *                    </ul>
     * @JcatTcAction Onboard model
     * @JcatTcActionResult Model package onboarded and available in MXE
     * @JcatTcAction Start a model deploymentent is running
     * @JcatTcActionResult Model is running
     * @JcatTcAction Onboard a flow which contains a previously started model
     * @JcatTcActionResult flow onboarded
     * @JcatTcAction Deploy a flow which contains a previously started model
     * @JcatTcActionResult flow deployed
     * @JcatTcAction Read model input json file
     * @JcatTcActionResult Model input lines are collected, which will be used for prediction
     * @JcatTcAction Start sending traffic with the selected send rate
     * @JcatTcActionResult Traffic sending started
     * @JcatTcAction Continously collect all the previously listed measurements from prometheus and store it in CSV file
     * @JcatTcActionResult Data got from prometheus and stored in CSV file
     * @JcatTcAction Stop traffic after measurementLength expired
     * @JcatTcActionResult Traffic stopped
     * @JcatTcAction Scale model deployment, increase instance with one
     * @JcatTcActionResult Model scaled out
     * @JcatTcAction Perform the previous model scaling, traffic starting and measurement steps for all of the instance number till we reached maxInstance with
     *               fix send rate
     * @JcatTcActionResult Measurement repeated for all of the instance number till we reached maxInstance
     * @JcatTcAction Stop data collection after measurementLength expired
     * @JcatTcActionResult Data collection stopped
     * @JcatTcAction Draw diagrams about the collected data. Separate diagram for all measurement type which contains measured values for each send rate
     * @JcatTcActionResult Diagrams generated
     * @JcatTcAction Stop previously started flow deployment
     * @JcatTcActionResult Flow deployment stopped
     * @JcatTcAction Delete onboarded flow
     * @JcatTcActionResult Flow deleted
     * @JcatTcAction Stop previously started model deployment
     * @JcatTcActionResult Model deployment stopped
     * @JcatTcAction Delete onboarded model
     * @JcatTcActionResult Model deleted
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-FLOW-SCALABILITY", testTitle = "MXE flow scalability test")
    @Parameters({ "packageName", "modelName", "modelVersion", "instanceName", "instance", "maxInstance", "flowName", "modelInputJsonPath", "maxRate",
            "measurementLength" })
    public void testMxeFlowScalability(String packageName, String modelName, String modelVersion, String instanceName, int instance, int maxInstance,
            String flowName, String modelInputJsonPath, int sendRate, int measurementLength) {
        // Not implemented yet
    }
}
