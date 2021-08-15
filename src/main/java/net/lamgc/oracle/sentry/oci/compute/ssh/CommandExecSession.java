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

/**
 * SSH 执行会话.
 * @author LamGC
 */
public final class CommandExecSession implements Closeable {

    private final ChannelExec channelExec;

    public CommandExecSession(ChannelExec channelExec) {
        this.channelExec = channelExec;
    }

    /**
     * 执行命令并等待程序执行完毕.
     * @throws IOException 如果发送命令失败则抛出异常.
     */
    public void exec() throws IOException {
        exec(false);
    }

    /**
     * 执行命令.
     * @param async 是否需要异步, 如果为异步, 则本方法不等待命令执行完成就返回, 适用于需要向程序输入的时候使用.
     * @throws IOException 如果发送命令时发生异常则抛出.
     */
    public void exec(boolean async) throws IOException {
        channelExec.open();
        if (!async) {
            waitFor();
        }
    }

    /**
     * 等待程序执行完毕.
     * <p> 该方法等同于 {@code waitFor(0L)}.
     */
    public void waitFor() {
        waitFor(0L);
    }

    /**
     * 等待程序执行完毕.
     * @param timeout 超时时间, 0 为无限等待(单位: 毫秒).
     */
    public void waitFor(long timeout) {
        channelExec.waitFor(EnumSet.of(ClientChannelEvent.EXIT_STATUS, ClientChannelEvent.EXIT_SIGNAL), timeout);
    }

    /**
     * 获取程序退出代码.
     * <p> 如果程序未执行完毕, 本方法将无法获取退出代码.
     * @return 如果程序执行完毕, 返回具体代码, 否则返回 {@code null}.
     */
    public Integer exitCode() {
        return channelExec.getExitStatus();
    }

    /**
     * 设置输入流.
     * <p> 设置待执行命令的输入流.
     */
    public void setIn(InputStream in) {
        channelExec.setIn(new InputStreamWrapper(in));
    }

    /**
     * 设置标准输出流.
     * <p> 对应待执行命令的 Stdout.
     */
    public void setOut(OutputStream out) {
        channelExec.setOut(new OutputStreamWrapper(out));
    }

    /**
     * 设置错误输出流.
     * <p> 如果命令使用到, 错误信息会从该输出流输出.
     */
    public void setErr(OutputStream err) {
        channelExec.setErr(new OutputStreamWrapper(err));
    }

    /**
     * 关闭命令执行会话.
     * @throws IOException 可能会引发的异常.
     */
    @Override
    public void close() throws IOException {
        channelExec.close();
    }
}
