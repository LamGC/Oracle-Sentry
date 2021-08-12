package net.lamgc.oracle.sentry.script.tools.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Http 响应.
 * @author LamGC
 */
public final class HttpAccessResponse {

    private final StatusLine statusLine;
    private final Locale locale;
    private final Map<String, String> headers = new ConcurrentHashMap<>();
    private final HttpEntity entity;

    HttpAccessResponse(HttpResponse response) {
        this.statusLine = response.getStatusLine();
        this.locale = response.getLocale();
        for (Header header : response.getAllHeaders()) {
            headers.put(header.getName(), header.getValue());
        }
        this.entity = response.getEntity();
    }

    public StatusLine getStatusLine() {
        return statusLine;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getContentToString() throws IOException {
        return EntityUtils.toString(entity);
    }

    public HttpEntity getEntity() {
        return entity;
    }

}
