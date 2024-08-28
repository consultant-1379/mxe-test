package com.ericsson.mxe.jcat.driver.kubernetes;

import static com.ericsson.mxe.jcat.command.Commands.kubectl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.mxe.jcat.command.CustomCommand;
import com.ericsson.mxe.jcat.command.result.CommandResult;
import com.ericsson.mxe.jcat.config.TestExecutionHost;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.util.Config;

public class KubernetesDriver implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesDriver.class);
    private static final int MAX_THREADS = 10;

    private final List<CompletableFuture<?>> futures;
    private final ExecutorService executorService;

    public static final String FIELDSELECTOR_RUNNING = "status.phase=Running";
    public static final String LABELSELECTOR_PROMETHEUS_SERVER = "app=prometheus-server";

    final CoreV1Api api;
    final TestExecutionHost testExecutionHost;

    public KubernetesDriver(TestExecutionHost testExecutionHost) {
        ApiClient apiClient;
        try {
            apiClient = Config.defaultClient();
        } catch (IOException e) {
            throw new IllegalStateException("Colud not init Kubernetes api client", e);
        }
        Configuration.setDefaultApiClient(apiClient);

        api = new CoreV1Api();
        this.testExecutionHost = testExecutionHost;
        futures = new ArrayList<>();
        executorService = Executors.newFixedThreadPool(MAX_THREADS, runnable -> {
            final Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setDaemon(true);
            return thread;
        });

    }

    public CoreV1Api getApi() {
        return api;
    }

    public CompletableFuture<CommandResult> portForward(V1Pod pod, int localPort, int podPort) {
        return portForward(pod.getMetadata().getName(), pod.getMetadata().getNamespace(), localPort, podPort);
    }

    public CompletableFuture<CommandResult> portForward(String podName, String nameSpace, int localPort, int podPort) {
        final CustomCommand portForwardCommand = kubectl().portForward("pod/" + podName, localPort, podPort, nameSpace);
        final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost);
        final CompletableFuture<CommandResult> future = CompletableFuture.supplyAsync(() -> mxeCliDriver.execute(portForwardCommand), executorService);

        LOGGER.info("Forwarding pod's port is ongoing [{}: {} -> {}]", podName, podPort, localPort);
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            LOGGER.error("Sleep was interrupted", e);
            Thread.currentThread().interrupt();
        }
        futures.add(future);
        return future;
    }

    @Override public void close() throws Exception {
        futures.forEach(future -> future.cancel(true));
        executorService.shutdown();
    }
}
