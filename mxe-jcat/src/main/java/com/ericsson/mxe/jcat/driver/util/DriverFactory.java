package com.ericsson.mxe.jcat.driver.util;

import com.ericsson.mxe.jcat.config.TestExecutionHost;
import com.ericsson.mxe.jcat.context.MxeJcatApplicationContextProvider;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.kubernetes.KubernetesDriver;
import com.ericsson.mxe.jcat.driver.prometheus.PrometheusDriver;

public final class DriverFactory {

    private DriverFactory() {}

    public static KubernetesDriver getKubernetesDriver(final TestExecutionHost testExecutionHost) {
        return new KubernetesDriver(testExecutionHost);
    }

    public static MxeCliDriver getMxeCliDriver(final TestExecutionHost testExecutionHost) {
        return MxeJcatApplicationContextProvider.getApplicationContext().getBean(MxeCliDriver.class, testExecutionHost);
    }

    public static PrometheusDriver getPrometheusDriver(final String url) {
        return new PrometheusDriver(url);
    }

    public static PrometheusDriver getPrometheusDriver(final String url, final String user, final String password) {
        return new PrometheusDriver(url, user, password);
    }

}
