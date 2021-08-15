package net.lamgc.oracle.sentry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author LamGC
 */
@Component("sentry.constants")
public final class Constants {

    public static Constants instance;

    private Constants() {
        instance = this;
    }

    @Value("${oracle.ssh.firstConnection.authenticationPolicy}")
    @NonNull
    private String firstConnectionPolicy;


    @NonNull
    public String getFirstConnectionPolicy() {
        return firstConnectionPolicy;
    }
}
