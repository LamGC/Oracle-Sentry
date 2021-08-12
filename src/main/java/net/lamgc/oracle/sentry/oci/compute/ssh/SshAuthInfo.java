package net.lamgc.oracle.sentry.oci.compute.ssh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    public String getUsername() {
        return username;
    }

    public PublicKey getServerKey() {
        return serverKey;
    }

    public void setServerKey(PublicKey serverKey) {
        this.serverKey = serverKey;
        if (this.provider != null) {
            this.provider.notifySaveInfo();
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    void setProvider(SshAuthIdentityProvider provider) {
        this.provider = provider;
    }

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

        public Class<? extends SshAuthInfo> getTargetClass() {
            return targetClass;
        }
    }

}
