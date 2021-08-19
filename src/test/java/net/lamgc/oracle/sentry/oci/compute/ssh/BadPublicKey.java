package net.lamgc.oracle.sentry.oci.compute.ssh;

import java.security.PublicKey;

public class BadPublicKey implements PublicKey {
    @Override
    public String getAlgorithm() {
        return null;
    }

    @Override
    public String getFormat() {
        return null;
    }

    @Override
    public byte[] getEncoded() {
        return new byte[0];
    }
}
