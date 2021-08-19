package net.lamgc.oracle.sentry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author LamGC
 */
@Component("sentry.constants")
public final class Constants {

    /**
     * 本类唯一实例, 请不要进行设置.
     */
    public static Constants instance;

    private Constants() {
        instance = this;
    }

    @Value("${oracle.ssh.firstConnection.authenticationPolicy}")
    @NonNull
    private String firstConnectionPolicy;


    /**
     * 获取 SSH 首次连接策略.
     * @return 返回策略值.
     */
    @NonNull
    public String getFirstConnectionPolicy() {
        return firstConnectionPolicy;
    }
}
