package net.lamgc.oracle.sentry.oci.compute.ssh;

import com.google.common.base.Strings;
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

public class InstanceSsh implements AutoCloseable {

    private final static Logger log = LoggerFactory.getLogger(InstanceSsh.class);

    private final ComputeInstance instance;
    private final SshAuthInfo authInfo;
    private final SshClient sshClient;

    public InstanceSsh(ComputeInstance instance, SshAuthInfo authInfo) {
        this.instance = Objects.requireNonNull(instance);
        this.authInfo = Objects.requireNonNull(authInfo);

        sshClient = SshClient.setUpDefaultClient();
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

    public SshSession createSession() throws IOException {
        Set<String> instancePublicIps = instance.network().getInstancePublicIp();
        if (instancePublicIps.stream().findFirst().isEmpty()) {
            throw new IllegalStateException("Instance has no public IP available.");
        }
        String connectUri = "ssh://" + authInfo.getUsername() + "@" + instancePublicIps.stream().findFirst().get() + ":22";
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
