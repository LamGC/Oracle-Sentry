package net.lamgc.oracle.sentry.oci.compute.ssh;

import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.server.forward.ForwardingFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 已配置转发过滤器.
 * <p> 根据应用配置, 选择是否允许转发指定类型的流量.
 * @author LamGC
 */
@Component("sentry.script.ssh.forwarding.filter")
public class ConfiguredForwardingFilter implements ForwardingFilter {

    @Value("oracle.ssh.forwarding.X11.enable")
    private String x11Enabled;

    @Value("oracle.ssh.forwarding.tcp.enable")
    private String tcpEnabled;

    @Value("oracle.ssh.forwarding.agent.enable")
    private String agentEnabled;

    @Override
    public boolean canForwardAgent(Session session, String requestType) {
        return Boolean.parseBoolean(agentEnabled);
    }

    @Override
    public boolean canListen(SshdSocketAddress address, Session session) {
        return Boolean.parseBoolean(tcpEnabled);
    }

    @Override
    public boolean canConnect(Type type, SshdSocketAddress address, Session session) {
        return Boolean.parseBoolean(tcpEnabled);
    }

    @Override
    public boolean canForwardX11(Session session, String requestType) {
        return Boolean.parseBoolean(x11Enabled);
    }
}
