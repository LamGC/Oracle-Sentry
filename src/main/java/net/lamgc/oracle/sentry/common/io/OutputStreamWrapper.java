package net.lamgc.oracle.sentry.common.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 输出流包装器.
 * <p> 准确来说只是屏蔽了 {@link OutputStream#close()} 而已,
 * 尝试修复 SSH 命令执行会话可能会关闭设置的输出流的问题.
 * @author LamGC
 */
public class OutputStreamWrapper extends OutputStream {
    
    private final OutputStream target;

    /**
     * 包装一个输出流.
     * @param target 目标输出流.
     */
    public OutputStreamWrapper(OutputStream target) {
        this.target = target;
    }

    @Override
    public void write(int b) throws IOException {
        target.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.target.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.target.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        this.target.flush();
    }
}
