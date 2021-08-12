package net.lamgc.oracle.sentry.script.tools.http;

import org.apache.http.client.HttpClient;

public class ScriptHttpClient {

    private final HttpClient httpClient;

    public ScriptHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * 打开一个连接.
     * @param url 要访问的 Url.
     */
    public HttpAccess create(String url) {
        return new HttpAccess(httpClient, url);
    }

}
