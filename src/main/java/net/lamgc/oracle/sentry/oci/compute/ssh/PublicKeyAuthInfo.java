package net.lamgc.oracle.sentry.oci.compute.ssh;

import java.io.File;

/**
 * 公钥登录认证配置.
 * @author LamGC
 */
public class PublicKeyAuthInfo extends SshAuthInfo{

    private File privateKeyPath;
    private String keyPassword;

    @Override
    public AuthType getType() {
        return AuthType.PUBLIC_KEY;
    }

    /**
     * 获取私钥路径.
     * <p> 注意: 该路径由 SSH 认证配置文件提供, 不保证私钥的存在.
     * @return 返回私钥所在路径.
     */
    public File getPrivateKeyPath() {
        return privateKeyPath;
    }

    /**
     * 设置私钥路径.
     * @param privateKeyPath 私钥路径.
     */
    public void setPrivateKeyPath(File privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    /**
     * 获取私钥密码.
     * @return 如果有, 返回非 {@code null} 值.
     */
    public String getKeyPassword() {
        return keyPassword;
    }

    /**
     * 设置私钥密码.
     * <p> 如果私钥存在密码但未提供密码, 将无法使用私钥验证会话.
     * @param keyPassword 私钥密码.
     */
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }
}
