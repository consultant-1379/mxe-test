package com.ericsson.mxe.jcat.command;

import com.ericsson.mxe.jcat.context.MxeJcatApplicationContextProvider;

public class Commands {

    private Commands() {}

    public static MxeFlowCommand mxeFlow() {
        return MxeJcatApplicationContextProvider.getApplicationContext().getBean(MxeFlowCommand.class);
    }

    public static MxeModelCommand mxeModel() {
        return MxeJcatApplicationContextProvider.getApplicationContext().getBean(MxeModelCommand.class);
    }

    public static KubectlCommand kubectl() {
        return MxeJcatApplicationContextProvider.getApplicationContext().getBean(KubectlCommand.class);
    }
}
