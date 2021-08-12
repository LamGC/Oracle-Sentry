package net.lamgc.oracle.sentry.oci.compute;

public enum InstanceAction {
    /**
     * 启动实例.
     */
    START("start"),
    /**
     * 硬停止实例.
     */
    STOP("stop"),
    /**
     * 硬重启实例.
     */
    RESET("reset"),
    /**
     * 软重启实例, 操作系统将按照正常的重启过程进行.
     */
    SOFT_RESET("softreset"),
    /**
     * 软停止实例, 操作系统将按照正常的关机过程进行.
     */
    SOFT_STOP("softstop")

    ;

    private final String actionValue;

    InstanceAction(String actionValue) {
        this.actionValue = actionValue;
    }

    public String getActionValue() {
        return actionValue;
    }
}
