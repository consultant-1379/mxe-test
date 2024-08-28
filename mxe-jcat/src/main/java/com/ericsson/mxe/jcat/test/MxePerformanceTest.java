package com.ericsson.mxe.jcat.test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ericsson.mxe.jcat.driver.kubernetes.KubernetesDriver;
import com.ericsson.mxe.jcat.driver.prometheus.PrometheusDriver;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1Pod;
import se.ericsson.jcat.fw.annotations.JcatClass;
import se.ericsson.jcat.fw.annotations.JcatMethod;
import se.ericsson.jcat.fw.annotations.Setup;
import se.ericsson.jcat.fw.annotations.Teardown;

/**
 * @JcatDocChapterDescription Chapter covering performance tests.
 */
@JcatClass(chapterName = "Performance Tests")
public class MxePerformanceTest extends MxeTestBase {

    private KubernetesDriver kubernetesDriver;
    private PrometheusDriver prometheusDriver;
    private V1Pod prometheusPod;

    @Setup
    public void setup() {
        setTestStepBegin("Setup");
        kubernetesDriver = DriverFactory.getKubernetesDriver(testExecutionHost);
        prometheusDriver = DriverFactory.getPrometheusDriver("http://localhost:9999");

        prometheusPod = null;
        try {
            prometheusPod = getPrometheusPod();
        } catch (ApiException e) {
            fail("Could not find Prometheus deployment");
        }

        assertNotNull("Could not find Prometheus deployment", prometheusPod);

        setTestInfoExpandable("Prometheus pod", prometheusPod.toString());
        setTestInfo("Setup finished");
        setTestStepEnd();
    }

    @Teardown
    public void teardown() {
        setTestStepBegin("Teardown");
        try {
            kubernetesDriver.close();
        } catch (Exception e) {
            setTestError("Teardown failed", e);
        }
        setTestInfo("Teardown finished");
        setTestStepEnd();
    }

    @Test
    @JcatMethod(testTag = "PROMETHEUS", testTitle = "Prometheus Test")
    public void prometheusTest() {
        setTestStepBegin("Example prometheus queries");
        final Instant now = Instant.now();
        final long start = now.minus(1, ChronoUnit.DAYS).toEpochMilli();
        final long end = now.toEpochMilli();

        setSubTestStep("Portforward for Prometheus");
        kubernetesDriver.portForward(prometheusPod, 9999, 9090);

        try {
            final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            setSubTestStep("Query");
            setTestInfo(mapper.writeValueAsString(prometheusDriver.query("system_cpu_usage{}")));

            setSubTestStep("Range query");
            setTestInfo(mapper.writeValueAsString(prometheusDriver.queryRange(start, end, "1s", "system_cpu_usage{}")));
        } catch (JsonProcessingException e) {
            setTestError("JsonProcessingException", e);
        }
        setTestStepEnd();
    }

    private V1Pod getPrometheusPod() throws ApiException {
        return kubernetesDriver
                .getApi()
                .listPodForAllNamespaces(null, KubernetesDriver.FIELDSELECTOR_RUNNING, false, KubernetesDriver.LABELSELECTOR_PROMETHEUS_SERVER, null, null,
                        null, null, null).getItems().stream().findFirst().orElse(null);
    }

    /**
     * @JcatTcDescription Performs performance test with a model deployment
     * @JcatTcPreconditions MXE cluster is up and running in a separated characteristics environment
     * @JcatTcInstruction The testcase is about sending data to a running model deployment with different rate and measure the following characteristics for
     *                    each send rate:<br/>
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
     *                    Since several preparatory steps are needed for execution of this performance test case, the followings are needed:
     *                    <ul>
     *                    <li>always execute with a predefined test suite</li>
     *                    </ul>
     *                    <b>Performance test parameters</b>
     *                    <ul>
     *                    <li><u>packageName:</u> Model package nam and path to be used during the test</li>
     *                    <li><u>modelName:</u> Name of the model after onboarding</li>
     *                    <li><u>modelVersion:</u>Version of the model</li>
     *                    <li><u>instanceName:</u>Name of the model deployment instance</li>
     *                    <li><u>instance:</u>Model deployment instance number</li>
     *                    <li><u>modelInputJsonPath:</u>Input json for model prediction</li>
     *                    <li><u>maxSendRate:</u>Maximum send rate we would like to reach during the measurement</li>
     *                    <li><u>sendRateSteps:</u>Steps to increase the send rate. The first value is the start send rate</li>
     *                    <li><u>measurementLength:</u>Length of a measurement step in seconds</li>
     *                    </ul>
     * @JcatTcAction Onboard model
     * @JcatTcActionResult Model package onboarded and available in MXE
     * @JcatTcAction Start a model deploymentent is running
     * @JcatTcActionResult Model is running
     * @JcatTcAction Read model input json file
     * @JcatTcActionResult Model input lines are collected, which will be used for prediction
     * @JcatTcAction Start sending traffic with the selected send rate (the first value is the sendRateSteps)
     * @JcatTcActionResult Traffic sending started
     * @JcatTcAction Continously collect all the previously listed measurements from prometheus and store it in CSV file
     * @JcatTcActionResult Data got from prometheus and stored in CSV file
     * @JcatTcAction Stop traffic after measurementLength expired
     * @JcatTcActionResult Traffic stopped
     * @JcatTcAction Perform the previous traffic starting and measurement steps for all of the test rates (increasing by sendRateSteps to maxSendRate)
     * @JcatTcActionResult Measurement repeated for all of the send rate levels
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
    @JcatMethod(testTag = "TEST-MXE-MODEL-PERFORMANCE", testTitle = "MXE model performance test")
    @Parameters({ "packageName", "modelName", "modelVersion", "instanceName", "instance", "modelInputJsonPath", "maxSendRate", "sendRateSteps",
            "measurementLength" })
    @SuppressWarnings("squid:S00107")
    public void testMxeModelPerformance(String packageName, String modelName, String modelVersion, String instanceName, int instance,
            String modelInputJsonPath, int maxSendRate, int sendRateSteps, int measurementLength) {
        // Not implemented yet
    }

    /**
     * @JcatTcDescription Performs performance test with a model deployment in a flow
     * @JcatTcPreconditions MXE cluster is up and running in a separated characteristics environment
     * @JcatTcInstruction The testcase is about sending data to a running model deployment in a flow with different rate and measure the following
     *                    characteristics for each send rate: <br/>
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
     *                    Since several preparatory steps are needed for execution of this performance test case, the followings are needed:
     *                    <ul>
     *                    <li>always execute with a predefined test suite</li>
     *                    </ul>
     *                    <b>Performance test parameters</b>
     *                    <ul>
     *                    <li><u>packageName:</u> Model package nam and path to be used during the test</li>
     *                    <li><u>modelName:</u> Name of the model after onboarding</li>
     *                    <li><u>modelVersion:</u>Version of the model</li>
     *                    <li><u>instanceName:</u>Name of the model deployment instance</li>
     *                    <li><u>instance:</u>Model deployment instance number</li>
     *                    <li><u>flowName:</u>Flow to onboard a deploy</li>
     *                    <li><u>modelInputJsonPath:</u>Input json for model prediction</li>
     *                    <li><u>maxSendRate:</u>Maximum send rate we would like to reach during the measurement</li>
     *                    <li><u>sendRateSteps:</u>Steps to increase the send rate. The first value is the start send rate</li>
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
     * @JcatTcAction Start sending traffic with the selected send rate (the first value is the sendRateSteps)
     * @JcatTcActionResult Traffic sending started
     * @JcatTcAction Continously collect all the previously listed measurements from prometheus and store it in CSV file
     * @JcatTcActionResult Data got from prometheus and stored in CSV file
     * @JcatTcAction Stop traffic after measurementLength expired
     * @JcatTcActionResult Traffic stopped
     * @JcatTcAction Perform the previous traffic starting and measurement steps for all of the test rates (increasing by sendRateSteps to maxSendRate)
     * @JcatTcActionResult Measurement repeated for all of the send rate levels
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
    @JcatMethod(testTag = "TEST-MXE-FLOW-PERFORMANCE", testTitle = "MXE flow performance test")
    @Parameters({ "packageName", "modelName", "modelVersion", "instanceName", "instance", "flowName", "modelInputJsonPath", "maxSendRate", "sendRateSteps",
            "measurementLength" })
    @SuppressWarnings("squid:S00107")
    public void testMxeFlowPerformance(String packageName, String modelName, String modelVersion, String instanceName, int instance, String flowName,
            String modelInputJsonPath, int maxSendRate, int sendRateSteps, int measurementLength) {
        // Not implemented yet
    }
}
