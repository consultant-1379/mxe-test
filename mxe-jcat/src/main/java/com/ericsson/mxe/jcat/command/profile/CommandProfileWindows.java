package com.ericsson.mxe.jcat.command.profile;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ericsson.mxe.jcat.command.KubectlCommand;
import com.ericsson.mxe.jcat.command.MxeFlowCommand;
import com.ericsson.mxe.jcat.command.MxeModelCommand;
import com.ericsson.mxe.jcat.command.windows.MxeFlowCommandWindows;
import com.ericsson.mxe.jcat.command.windows.MxeModelCommandWindows;

@Component
public class CommandProfileWindows {

    @Bean
    @Profile(CommandProfiles.WINDOWS)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public MxeFlowCommand mxeFlowCommand() {
        return new MxeFlowCommandWindows();
    }

    @Bean
    @Profile(CommandProfiles.WINDOWS)
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public MxeModelCommand mxeModelCommand() {
        return new MxeModelCommandWindows();
    }

    @Bean
    @Profile(CommandProfiles.WINDOWS)
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public KubectlCommand kubectlCommand() {
        throw new NotImplementedException("Not implemented");
    }
}
