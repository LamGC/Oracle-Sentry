package net.lamgc.oracle.sentry.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 输入流包装器.
 * <p> 准确来说只是屏蔽了 {@link InputStream#close()} 而已,
 * 尝试修复 SSH 命令执行会话可能会关闭设置的输入流的问题.
 * @author LamGC
 */
public class InputStreamWrapper extends InputStream {
    
    private final InputStream source;

    /**
     * 包装一个输入流.
     * @param source 输入源.
     */
    public InputStreamWrapper(InputStream source) {
        this.source = source;
    }
    
    @Override
    public int read() throws IOException {
        return this.source.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.source.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return this.source.read(b, off, len);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return this.source.readAllBytes();
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        return this.source.readNBytes(len);
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        return this.source.readNBytes(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return this.source.skip(n);
    }

    @Override
    public void skipNBytes(long n) throws IOException {
        this.source.skipNBytes(n);
    }

    @Override
    public int available() throws IOException {
        return this.source.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        this.source.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        this.source.reset();
    }

    @Override
    public boolean markSupported() {
        return this.source.markSupported();
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        return this.source.transferTo(out);
    }
}
