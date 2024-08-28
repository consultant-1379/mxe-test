package com.ericsson.mxe.jcat.command;

public abstract class MxeModelCommand extends MxeCommand {

    private static final String PARAMETER_TEMPLATE_LIST = " list";
    private static final String PARAMETER_TEMPLATE_LIST_STARTED = " list started";
    private static final String PARAMETER_TEMPLATE_LIST_ONBOARDED = " list onboarded";
    private static final String PARAMETER_TEMPLATE_PACKAGE = " package --name %s --source %s";
    private static final String PARAMETER_TEMPLATE_ONBOARD_WITHOUT_DESCRIPTION = " onboard --name %s --version %s --packagename %s";
    private static final String PARAMETER_TEMPLATE_ONBOARD_WITH_DESCRIPTION = " onboard --name %s --version %s --packagename %s --description %s";
    private static final String PARAMETER_TEMPLATE_DELETE = " delete --name %s:%s";
    private static final String PARAMETER_TEMPLATE_START = " start --name %s --packagename %s --instances %d";
    private static final String PARAMETER_TEMPLATE_START_SHORT = " start --packagename %s --instances %d";
    private static final String PARAMETER_TEMPLATE_SCALE = " scale --name %s --instances %d";
    private static final String PARAMETER_TEMPLATE_UPGRADE = " upgrade --id %s --targetversion %s";
    private static final String PARAMETER_TEMPLATE_STOP = " stop --name %s";
    private static final String PARAMETER_TEMPLATE_VERSION = " version";

    public MxeModelCommand(String command) {
        super(command);
    }

    public MxeModelCommand list() {
        setParameter(PARAMETER_TEMPLATE_LIST);
        return this;
    }

    public MxeModelCommand listStarted() {
        setParameter(PARAMETER_TEMPLATE_LIST_STARTED);
        return this;
    }

    public MxeModelCommand listOnboarded() {
        setParameter(PARAMETER_TEMPLATE_LIST_ONBOARDED);
        return this;
    }

    public MxeModelCommand pack(final String name, final String source) {
        setParameter(String.format(PARAMETER_TEMPLATE_PACKAGE, name, source));
        return this;
    }

    public MxeModelCommand onboard(final String name, final String version, final String packagename) {
        setParameter(String.format(PARAMETER_TEMPLATE_ONBOARD_WITHOUT_DESCRIPTION, name, version, packagename));
        return this;
    }

    public MxeModelCommand onboard(final String name, final String version, final String packagename, final String description) {
        setParameter(String.format(PARAMETER_TEMPLATE_ONBOARD_WITH_DESCRIPTION, name, version, packagename, description));
        return this;
    }

    public MxeModelCommand delete(final String name, final String version) {
        setParameter(String.format(PARAMETER_TEMPLATE_DELETE, name, version));
        return this;
    }

    public MxeModelCommand start(final String name, final String packageName, final int instances) {
        setParameter(String.format(PARAMETER_TEMPLATE_START, name, packageName, instances));
        return this;
    }

    public MxeModelCommand start(final String packageName, final int instances) {
        setParameter(String.format(PARAMETER_TEMPLATE_START_SHORT, packageName, instances));
        return this;
    }

    public MxeModelCommand stop(final String name) {
        setParameter(String.format(PARAMETER_TEMPLATE_STOP, name));
        return this;
    }

    public MxeModelCommand scale(final String name, final int instances) {
        setParameter(String.format(PARAMETER_TEMPLATE_SCALE, name, instances));
        return this;
    }

    public MxeModelCommand upgrade(final String id, final String targetversion) {
        setParameter(String.format(PARAMETER_TEMPLATE_UPGRADE, id, targetversion));
        return this;
    }

    public MxeModelCommand version() {
        setParameter(PARAMETER_TEMPLATE_VERSION);
        return this;
    }
}
