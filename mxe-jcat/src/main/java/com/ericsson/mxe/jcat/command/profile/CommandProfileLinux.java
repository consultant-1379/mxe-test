package com.ericsson.mxe.jcat.command.profile;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ericsson.mxe.jcat.command.KubectlCommand;
import com.ericsson.mxe.jcat.command.MxeFlowCommand;
import com.ericsson.mxe.jcat.command.MxeModelCommand;
import com.ericsson.mxe.jcat.command.linux.KubectlCommandLinux;
import com.ericsson.mxe.jcat.command.linux.MxeFlowCommandLinux;
import com.ericsson.mxe.jcat.command.linux.MxeModelCommandLinux;

@Component
public class CommandProfileLinux {

    @Bean
    @Profile(CommandProfiles.LINUX)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public MxeFlowCommand mxeFlowCommand() {
        return new MxeFlowCommandLinux();
    }

    @Bean
    @Profile(CommandProfiles.LINUX)
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public MxeModelCommand mxeModelCommand() {
        return new MxeModelCommandLinux();
    }

    @Bean
    @Profile(CommandProfiles.LINUX)
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public KubectlCommand kubectlCommand() {
        return new KubectlCommandLinux();
    }

}
