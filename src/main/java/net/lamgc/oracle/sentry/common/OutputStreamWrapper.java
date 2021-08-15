package net.lamgc.oracle.sentry.common;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamWrapper extends OutputStream {
    
    private final OutputStream target;
    
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
