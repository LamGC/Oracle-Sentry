package net.lamgc.oracle.sentry.oci.compute.ssh;

/**
 * @author LamGC
 */
public class PasswordAuthInfo extends SshAuthInfo {

    private String password;

    @Override
    public AuthType getType() {
        return AuthType.PASSWORD;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
