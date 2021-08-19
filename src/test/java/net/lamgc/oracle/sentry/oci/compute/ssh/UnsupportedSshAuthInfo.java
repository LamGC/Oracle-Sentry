package net.lamgc.oracle.sentry.oci.compute.ssh;

public class UnsupportedSshAuthInfo extends SshAuthInfo {

    private final boolean returnType;

    public UnsupportedSshAuthInfo(boolean returnType) {
        this.returnType = returnType;
    }

    @Override
    public AuthType getType() {
        return returnType ? AuthType.PASSWORD : null;
    }
}
