package net.lamgc.oracle.sentry.script.tools.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Http 访问对象.
 * <p> 该对象可以复用.
 * @author LamGC
 */
public class HttpAccess {

    private final HttpClient client;
    private final String url;

    HttpAccess(HttpClient client, String url) {
        this.client = client;
        this.url = url;
    }

    /**
     * 以 Get 方法发起 Http 请求.
     * @return 返回 Http 响应对象.
     * @throws IOException 当请求发送失败时抛出异常.
     */
    public HttpAccessResponse get() throws IOException {
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);
        return new HttpAccessResponse(response);
    }

    /**
     * 以 Post 方法发起 Http 请求.
     * @param body Post 请求体.
     * @return 返回 Http 响应对象.
     * @throws IOException 当请求发送失败时抛出异常.
     */
    public HttpAccessResponse post(String body) throws IOException {
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        HttpResponse response = client.execute(request);
        return new HttpAccessResponse(response);
    }


}
