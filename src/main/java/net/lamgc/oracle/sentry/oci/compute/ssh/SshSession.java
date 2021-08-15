package net.lamgc.oracle.sentry.oci.compute.ssh;

import org.apache.sshd.client.session.ClientSession;

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

    public SshSession(ClientSession clientSession) {
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
     * 关闭 SSH 连接会话, 该连接会话所属的其他会话将会一同被关闭.
     * @throws IOException 关闭失败时抛出异常,
     *          可能是由于连接已经被关闭而引起(具体看异常信息).
     */
    @Override
    public void close() throws IOException {
        clientSession.close();
    }
}
