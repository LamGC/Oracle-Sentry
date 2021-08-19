package net.lamgc.oracle.sentry.script.tools.http;

import org.apache.http.client.HttpClient;

/**
 * 创建脚本使用的 HttpClient 包装对象.
 * <p> 可根据脚本需要优化和简化步骤.
 * @author LamGC
 */
public class ScriptHttpClient {

    private final HttpClient httpClient;

    /**
     * 包装并构造一个脚本 Http 客户端.
     * @param httpClient 原始 Http 客户端.
     */
    public ScriptHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * 打开一个连接.
     * @param url 要访问的 Url.
     * @return 返回 Http 访问对象(可重复使用).
     */
    public HttpAccess create(String url) {
        return new HttpAccess(httpClient, url);
    }

}
