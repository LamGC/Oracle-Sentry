package net.lamgc.oracle.sentry.oci.compute.ssh;

import org.apache.sshd.common.config.keys.FilePasswordProvider;

import java.io.File;
import java.security.KeyPair;

public class PublicKeyAuthInfo extends SshAuthInfo{

    private File privateKeyPath;
    private String keyPassword;

    @Override
    public AuthType getType() {
        return AuthType.PUBLIC_KEY;
    }

    public File getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(File privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }
}
