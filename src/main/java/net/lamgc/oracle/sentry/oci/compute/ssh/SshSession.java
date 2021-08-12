package net.lamgc.oracle.sentry.oci.compute.ssh;

import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;

import java.io.Closeable;
import java.io.IOException;

public class SshSession implements Closeable {

    private final ClientSession clientSession;

    public SshSession(ClientSession clientSession) {
        this.clientSession = clientSession;
    }

    public SshExecChannel execCommand(String command) throws IOException {
        return new SshExecChannel(clientSession.createExecChannel(command));
    }

    @Override
    public void close() throws IOException {
        clientSession.close();
    }
}
