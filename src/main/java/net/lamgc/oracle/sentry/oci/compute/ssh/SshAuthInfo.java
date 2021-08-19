package net.lamgc.oracle.sentry.oci.compute.ssh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;

/**
 * Ssh 认证信息.
 * @author LamGC
 */
@SuppressWarnings("unused")
public abstract class SshAuthInfo {

    private final static Logger log = LoggerFactory.getLogger(SshAuthInfo.class);

    private String username;
    /**
     * 使用 Sha256 计算的密钥指纹.
     */
    private PublicKey serverKey;

    private SshAuthIdentityProvider provider;

    /**
     * 获取认证类型.
     * @return 返回认证类型.
     */
    public abstract AuthType getType();

    /**
     * 获取 SSH 登录用户名.
     * @return 返回 SSH 登录用户名.
     */
    public String getUsername() {
        return username;
    }

    /**
     * 获取服务器公钥.
     * <p> 用于认证服务器身份, 在首次登录成功后设置.
     * @return 如果之前认证成功并保存过, 则不为 {@code null}, 否则需要进行首次连接确认.
     */
    public PublicKey getServerKey() {
        return serverKey;
    }

    /**
     * 设置服务器公钥.
     * <p> 如果本对象有关联的 {@link SshAuthIdentityProvider}, 则会通知 Provider 保存 SSH 认证配置文件.
     * @param serverKey 服务器公钥.
     */
    public void setServerKey(PublicKey serverKey) {
        this.serverKey = serverKey;
        if (this.provider != null) {
            this.provider.notifySaveInfo();
        }
    }

    /**
     * 设置 SSH 登录用户名.
     * @param username 登录 SSH 的用户名.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 设置 SSH 认证配置提供器.
     * <p> 设置后, 可在首次连接认证通过后, 保存服务器公钥到文件中.
     * @param provider 所属提供器对象.
     */
    void setProvider(SshAuthIdentityProvider provider) {
        this.provider = provider;
    }

    /**
     * 认证类型.
     * <p> 如果没有所需认证类型, 就是没支持.
     */
    public enum AuthType {
        /**
         * 密码认证.
         */
        PASSWORD(PasswordAuthInfo.class),
        /**
         * 公钥认证
         */
        PUBLIC_KEY(PublicKeyAuthInfo.class);

        private final Class<? extends SshAuthInfo> targetClass;

        AuthType(Class<? extends SshAuthInfo> targetClass) {
            this.targetClass = targetClass;
        }

        /**
         * 获取类型所属的认证配置类.
         * @return 返回认证配置类.
         */
        public Class<? extends SshAuthInfo> getTargetClass() {
            return targetClass;
        }
    }

}
