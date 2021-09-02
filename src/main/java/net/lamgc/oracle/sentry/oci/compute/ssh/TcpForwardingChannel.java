package net.lamgc.oracle.sentry.oci.compute.ssh;

import org.apache.sshd.client.session.forward.ExplicitPortForwardingTracker;
import org.apache.sshd.common.util.net.SshdSocketAddress;

/**
 * TCP 隧道.
 * <p> 可通过该对象管理隧道.
 * @author LamGC
 */
public class TcpForwardingChannel implements AutoCloseable {

    private final ExplicitPortForwardingTracker tracker;

    TcpForwardingChannel(ExplicitPortForwardingTracker tracker) {
        this.tracker = tracker;
    }

    /**
     * 是否为本地转发.
     * @return 如果是本地转发, 则为 {@code true}, 否则返回 {@code false}, 代表远端转发.
     */
    public boolean isLocalForwarding() {
        return tracker.isLocalForwarding();
    }

    /**
     * 隧道是否已打开.
     * @return 如果已经打开, 返回 {@code true}, 否则返回 {@code false} 表示已关闭.
     */
    public boolean isOpen() {
        return tracker.isOpen();
    }

    /**
     * 获取本地地址.
     * @return 获取本地连接地址.
     */
    public SshdSocketAddress getLocalAddress() {
        return tracker.getLocalAddress();
    }

    /**
     * 获取远端地址.
     * @return 获取远端地址.
     */
    public SshdSocketAddress getRemoteAddress() {
        return tracker.getRemoteAddress();
    }

    /**
     * 获取监听绑定地址.
     * @return 返回监听绑定地址.
     */
    public SshdSocketAddress getBoundAddress() {
        return tracker.getBoundAddress();
    }

    @Override
    public void close() throws Exception {
        tracker.close();
    }
}
