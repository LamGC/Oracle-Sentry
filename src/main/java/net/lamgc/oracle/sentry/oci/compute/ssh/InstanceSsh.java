package net.lamgc.oracle.sentry.oci.compute.ssh;

import com.google.common.base.Strings;
import net.lamgc.oracle.sentry.Constants;
import net.lamgc.oracle.sentry.oci.compute.ComputeInstance;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 实例 SSH 客户端.
 * <p> 包装并简化了 SSH 会话的创建流程.
 * @author LamGC
 */
@SuppressWarnings("unused")
public class InstanceSsh implements AutoCloseable {

    private final static Logger log = LoggerFactory.getLogger(InstanceSsh.class);

    private final ComputeInstance instance;
    private final SshAuthInfo authInfo;
    private final SshClient sshClient;

    /**
     * 创建连接实例用的 SSH 客户端.
     * @param instance SSH 客户端对应的计算实例.
     * @param authInfo SSH 认证配置.
     */
    public InstanceSsh(ComputeInstance instance, SshAuthInfo authInfo) {
        this.instance = Objects.requireNonNull(instance);
        this.authInfo = Objects.requireNonNull(authInfo);

        sshClient = SshClient.setUpDefaultClient();
        sshClient.setForwardingFilter(Constants.instance.getForwardingFilter());
        sshClient.setServerKeyVerifier(new OracleInstanceServerKeyVerifier(instance, authInfo));
        if (authInfo instanceof PublicKeyAuthInfo info) {
            sshClient.setKeyIdentityProvider(new FileKeyPairProvider(info.getPrivateKeyPath().toPath()));
            if (!Strings.isNullOrEmpty(info.getKeyPassword())) {
                sshClient.setFilePasswordProvider(FilePasswordProvider.of(info.getKeyPassword()));
            }
        } else if (authInfo instanceof PasswordAuthInfo info) {
            sshClient.addPasswordIdentity(info.getPassword());
        } else {
            throw new IllegalArgumentException("Unsupported authentication type");
        }
        sshClient.start();
    }

    /**
     * 创建 SSH 会话.
     * <p> 允许创建多个 SSH 会话.
     * @return 返回新的 SSH 会话.
     * @throws IOException 会话创建失败时将抛出异常.
     */
    public SshSession createSession() throws IOException {
        Set<String> instancePublicIps = instance.network().getInstancePublicIp();
        if (instancePublicIps.stream().findFirst().isEmpty()) {
            throw new IllegalStateException("Instance has no public IP available.");
        }
        String connectUri = "ssh://" + authInfo.getUsername() + "@" +
                instancePublicIps.stream().findFirst().get() + ":" + authInfo.getPort();
        log.info("SSH 正在连接: {}", connectUri);
        ConnectFuture connect = sshClient.connect(connectUri);
        connect.verify();
        if (!connect.isConnected()) {
            if (connect.getException() != null) {
                throw new IOException(connect.getException());
            }
            throw new IOException("A connection to the server could not be established for an unknown reason.");
        }
        ClientSession clientSession = connect.getClientSession();
        AuthFuture auth = clientSession.auth();
        auth.verify(20, TimeUnit.SECONDS);
        if (auth.isSuccess()) {
            return new SshSession(clientSession);
        } else {
            if (auth.isFailure()) {
                clientSession.close();
                throw new IOException("Authentication with server failed.", clientSession.auth().getException());
            } else if (auth.isCanceled()) {
                clientSession.close();
                throw new IOException("Authentication cancelled.", clientSession.auth().getException());
            }
            throw new IOException("Authentication timeout.");
        }
    }

    @Override
    public void close() {
        sshClient.stop();
    }
}
