package com.ericsson.mxe.jcat.command;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;

import com.ericsson.mxe.jcat.command.profile.CommandProfiles;
import com.ericsson.mxe.jcat.command.windows.MxeFlowCommandWindows;
import com.ericsson.mxe.jcat.command.windows.MxeModelCommandWindows;
import com.ericsson.mxe.jcat.context.MxeJcatApplicationContextProvider;

@RunWith(BlockJUnit4ClassRunner.class)
public class MxeJcatApplicationContextWindowsProfileTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MxeJcatApplicationContextWindowsProfileTest.class);

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void beforeClassSetup() {
        LOGGER.info("[testClass: {}, commandProfile: {}]", AutoWiredApplicationContextLinuxProfileTest.class.getSimpleName(), CommandProfiles.WINDOWS);
    }

    @Before
    public void beforeMethod() {
        LOGGER.info("[testMethod: {}]", testName.getMethodName());
        MxeJcatApplicationContextProvider.setActiveProfiles(CommandProfiles.WINDOWS);
    }

    @Test
    public void testContextShouldInitProperMxeFlowCommandType() {
        assertThat(Commands.mxeFlow(), instanceOf(MxeFlowCommandWindows.class));
    }

    @Test
    public void testContextShouldInitProperMxeModelCommandType() {
        assertThat(Commands.mxeModel(), instanceOf(MxeModelCommandWindows.class));
    }

}
