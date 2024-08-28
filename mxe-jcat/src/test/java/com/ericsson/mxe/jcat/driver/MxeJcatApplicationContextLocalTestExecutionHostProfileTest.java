package com.ericsson.mxe.jcat.driver;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;

import com.ericsson.mxe.jcat.config.TestExecutionHost;
import com.ericsson.mxe.jcat.config.User;
import com.ericsson.mxe.jcat.context.MxeJcatApplicationContextProvider;
import com.ericsson.mxe.jcat.driver.cli.LocalMxeCliDriver;
import com.ericsson.mxe.jcat.driver.profile.TestExecutionHostProfiles;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;

@RunWith(BlockJUnit4ClassRunner.class)
public class MxeJcatApplicationContextLocalTestExecutionHostProfileTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MxeJcatApplicationContextLocalTestExecutionHostProfileTest.class);

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void beforeClassSetup() {
        LOGGER.info("[testClass: {}, commandProfile: {}]", MxeJcatApplicationContextLocalTestExecutionHostProfileTest.class.getSimpleName(),
                TestExecutionHostProfiles.LOCAL);
    }

    @Before
    public void beforeMethod() {
        LOGGER.info("[testMethod: {}]", testName.getMethodName());
        MxeJcatApplicationContextProvider.setActiveProfiles(TestExecutionHostProfiles.LOCAL);
    }

    @Test
    public void testContextShouldInitProperMxeCliDriverType() {
        final TestExecutionHost testExecutionHost = mock(TestExecutionHost.class);
        final User user = mock(User.class);

        when(testExecutionHost.getHost()).thenReturn("");
        when(testExecutionHost.getUser()).thenReturn(user);

        assertThat(DriverFactory.getMxeCliDriver(testExecutionHost), instanceOf(LocalMxeCliDriver.class));
    }

}
