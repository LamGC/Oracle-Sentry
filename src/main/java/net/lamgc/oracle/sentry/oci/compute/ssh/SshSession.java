package net.lamgc.oracle.sentry.oci.compute.ssh;

import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.session.forward.ExplicitPortForwardingTracker;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.sftp.client.SftpClientFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * SSH 连接会话.
 * <p> 可与服务器创建多个 SSH 连接会话.
 * @author LamGC
 */
@SuppressWarnings("unused")
public class SshSession implements Closeable {

    private final ClientSession clientSession;

    /**
     * 创建新的 SSH 会话.
     * @param clientSession 原始 SSH 会话.
     */
    SshSession(ClientSession clientSession) {
        this.clientSession = clientSession;
    }

    /**
     * 创建命令执行会话.
     * @param command 待执行的命令.
     * @return 返回命令执行会话.
     * @throws IOException 当会话创建失败时抛出异常.
     */
    public CommandExecSession createExecSession(String command) throws IOException {
        return new CommandExecSession(clientSession.createExecChannel(command));
    }

    /**
     * 创建 Sftp 会话.
     * <p> 可通过会话操作 Sftp.
     * @return 返回 Sftp 会话.
     * @throws IOException 如果创建失败, 将抛出异常.
     */
    public SftpSession createSftpSession() throws IOException {
        SftpClientFactory factory = SftpClientFactory.instance();
        return new SftpSession(factory.createSftpClient(clientSession));
    }

    /**
     * 创建本地 TCP 转发隧道.
     * <p> 该隧道为方向为 "本地-&gt;远端" (本地发起连接转发至远端端口).
     * @param localPort 本地监听端口.
     * @param remotePort 远端目标端口.
     * @return 返回 TCP 转发通道对象, 可获取通道信息和关闭通道.
     * @throws IOException 当操作失败时抛出该异常.
     */
    public TcpForwardingChannel createLocalTcpForwarding(int localPort, int remotePort) throws IOException {
        ExplicitPortForwardingTracker tracker = clientSession
                .createLocalPortForwardingTracker(localPort, new SshdSocketAddress(remotePort));
        return new TcpForwardingChannel(tracker);
    }

    /**
     * 创建远端 TCP 转发隧道.
     * <p> 该隧道为方向为 "本地&lt;-远端" (远端服务器发起连接转发至本地端口).
     * @param remotePort 远端监听端口号, 该端口为远端服务连接转发的端口号.
     * @param localPort 本地连接端口号, 该端口为本地服务端的端口号.
     * @return 返回 Tcp 转发通道对象, 用于管理转发通道.
     * @throws IOException 当操作失败时抛出异常.
     */
    public TcpForwardingChannel createRemoteTcpForwarding(int remotePort, int localPort) throws IOException {
        ExplicitPortForwardingTracker tracker =
                clientSession.createRemotePortForwardingTracker(
                        new SshdSocketAddress(remotePort), new SshdSocketAddress(localPort));
        return new TcpForwardingChannel(tracker);
    }

    /**
     * 关闭 SSH 连接会话, 该连接会话所属的其他会话将会一同被关闭.
     * @throws IOException 关闭失败时抛出异常,
     *          可能是由于连接已经被关闭而引起(具体看异常信息).
     */
    @Override
    public void close() throws IOException {
        clientSession.close();
    }
}
