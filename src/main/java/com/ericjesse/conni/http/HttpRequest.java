package com.ericjesse.conni.http;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Request to check the connectivity on the Internet.
 */
public class HttpRequest implements Serializable {

    public static final String USER_AGENT_KEY = "User-Agent";

    public static final String DEFAULT_USER_AGENT = "Conni (com.ericjesse.conni)";

    public static final String ACCEPT_ENCODING_KEY = "Accept-Encoding";

    public static final String DEFAULT_ACCEPT_ENCODING = "gzip";

    public static final String CONTENT_TYPE_KEY = "Content-Type";

    public static final String ACCEPT_KEY = "Accept";

    // Package visibility for test purpose.
    static final HttpMethod DEFAULT_METHOD = HttpMethod.GET;

    // Package visibility for test purpose.
    static final HttpContentType DEFAULT_CONTENT = HttpContentType.JSON;

    private HttpMethod method = DEFAULT_METHOD;

    private String url;

    private String body = null;

    private HttpContentType contentType = DEFAULT_CONTENT;

    private Map<String, List<String>> headers = new HashMap<>();

    /**
     * Constructor to create a GET request using JSON contents.
     *
     * @param url URL to request.
     */
    public HttpRequest(final String url) {
        this(url, null);
    }

    /**
     * Constructor to create a GET request using JSON contents and customized HTTP headers.
     *
     * @param url     URL to request.
     * @param headers Keys and values to add in the request as HTTP headers.
     */
    public HttpRequest(final String url, final HttpHeader... headers) {
        this(DEFAULT_METHOD, url, null, DEFAULT_CONTENT, headers);
    }

    /**
     * Full constructor to create a full customized request.
     *
     * @param method      The HTTP method to use.
     * @param url         URL to request.
     * @param body        The body of the request, as a string. It can be any marshalled object or simply {@code null}.
     * @param contentType The content type of the request and the accepted content type for the response.
     * @param headers     Keys and values to add in the request as HTTP headers.
     */
    public HttpRequest(final HttpMethod method, final String url, final String body, final HttpContentType contentType,
            final HttpHeader... headers) {
        this.method = method;
        this.url = url;
        this.body = body;
        this.contentType = contentType;

        if (null != headers) {
            // One header can be set several times. Thus, a list is created for each header key.
            for (HttpHeader header : headers) {
                this.headers.compute(header.getKey(), (key, value) -> (value == null) ? listOfOne(header.getValues()) :
                        addToList(value, header.getValues()));
            }
        }
        // Put the default values in place.
        addHeaderIfNotExists(USER_AGENT_KEY, DEFAULT_USER_AGENT);
        addHeaderIfNotExists(ACCEPT_ENCODING_KEY, DEFAULT_ACCEPT_ENCODING);
        addHeaderIfNotExists(CONTENT_TYPE_KEY, this.contentType.getValue());
        addHeaderIfNotExists(ACCEPT_KEY, this.contentType.getValue());
    }

    private void addHeaderIfNotExists(final String key, final String value) {
        if (!this.headers.containsKey(key)) {
            this.headers.put(key, Arrays.asList(value));
        }
    }

    private <T> List<T> listOfOne(final T[] values) {
        final List<T> list = new LinkedList<>();
        return addToList(list, values);
    }

    private <T> List<T> addToList(final List<T> list, final T[] values) {
        if (null != values) {
            for (T value : values) {
                list.add(value);
            }
        }
        return list;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(final HttpMethod method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(final String body) {
        this.body = body;
    }

    public HttpContentType getContentType() {
        return contentType;
    }

    public void setContentType(final HttpContentType contentType) {
        this.contentType = contentType;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

}
