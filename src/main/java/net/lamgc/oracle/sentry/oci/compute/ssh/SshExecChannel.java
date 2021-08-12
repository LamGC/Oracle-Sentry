package net.lamgc.oracle.sentry.oci.compute.ssh;

import net.lamgc.oracle.sentry.common.InputStreamWrapper;
import net.lamgc.oracle.sentry.common.OutputStreamWrapper;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;

public final class SshExecChannel implements Closeable {

    private final ChannelExec channelExec;

    public SshExecChannel(ChannelExec channelExec) {
        this.channelExec = channelExec;
    }

    public void exec() throws IOException {
        channelExec.open();
        channelExec.waitFor(EnumSet.of(ClientChannelEvent.EXIT_STATUS, ClientChannelEvent.EXIT_SIGNAL), 0L);
    }

    public Integer exitCode() {
        return channelExec.getExitStatus();
    }

    public void setIn(InputStream in) {
        channelExec.setIn(new InputStreamWrapper(in));
    }

    public void setOut(OutputStream out) {
        channelExec.setOut(new OutputStreamWrapper(out));
    }

    public void setErr(OutputStream err) {
        channelExec.setErr(new OutputStreamWrapper(err));
    }

    @Override
    public void close() throws IOException {
        channelExec.close();
    }
}
