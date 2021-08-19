package net.lamgc.oracle.sentry.script.tools.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;

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

    /**
     * 获取响应状态行.
     * @return 返回响应状态行, 包括响应码和信息.
     */
    public StatusLine getStatusLine() {
        return statusLine;
    }

    /**
     * 获取语言.
     * @return 返回 Locale 对象.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * 将 ResponseBody 转为字符串并返回.
     * @return 返回字符串形式的响应体.
     * @throws IOException 当接收失败时抛出异常.
     */
    public String getContentToString() throws IOException {
        return EntityUtils.toString(entity);
    }

    /**
     * 获取响应体实体, 可手动接收 Http Response Body.
     * @return 返回 Http 实体.
     */
    public HttpEntity getEntity() {
        return entity;
    }

    /**
     * 获取 Header.
     * @param name Header 名称.
     * @return 如果存在, 返回相应值, 否则返回 {@code null}.
     */
    public String getHeader(String name) {
        return headers.get(name);
    }
}
