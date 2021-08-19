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

    /**
     * 获取 SSH 登录密码.
     * @return 返回登录密码.
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置 SSH 登录密码.
     * @param password 新的登录密码.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
