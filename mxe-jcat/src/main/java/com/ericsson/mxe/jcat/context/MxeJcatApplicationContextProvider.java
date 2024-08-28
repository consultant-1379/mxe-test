package com.ericsson.mxe.jcat.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.ericsson.mxe.jcat.command.profile.CommandProfiles;
import com.ericsson.mxe.jcat.driver.profile.TestExecutionHostProfiles;

public class MxeJcatApplicationContextProvider {

    private static AnnotationConfigApplicationContext applicationContext;
    private static final Class<?>[] configurationClasses = { CommandProfiles.class, TestExecutionHostProfiles.class };
    private static final List<String> activeProfiles = new ArrayList<>();

    private MxeJcatApplicationContextProvider() {}

    private static void newApplicationContext() {
        applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(configurationClasses);
    }

    public static synchronized AnnotationConfigApplicationContext getApplicationContext() {
        if (Objects.isNull(applicationContext)) {
            setActiveProfiles(CommandProfiles.DEFAULT);
        }

        return applicationContext;
    }

    public static synchronized AnnotationConfigApplicationContext setActiveProfiles(String... profiles) {
        newApplicationContext();

        applicationContext.getEnvironment().setActiveProfiles(profiles);
        applicationContext.refresh();

        activeProfiles.clear();
        activeProfiles.addAll(Arrays.asList(profiles));

        return applicationContext;
    }

}
