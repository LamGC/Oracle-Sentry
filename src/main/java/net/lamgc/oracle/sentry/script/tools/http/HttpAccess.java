package net.lamgc.oracle.sentry.script.tools.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author LamGC
 */
public class HttpAccess {

    private final HttpClient client;
    private final String url;

    HttpAccess(HttpClient client, String url) {
        this.client = client;
        this.url = url;
    }

    public HttpAccessResponse get() throws IOException {
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);
        return new HttpAccessResponse(response);
    }

    public HttpAccessResponse post(String body) throws IOException {
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        HttpResponse response = client.execute(request);
        return new HttpAccessResponse(response);
    }


}
