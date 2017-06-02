package com.ericjesse.conni.http;

/**
 * Wrapper to easily pass headers to build a HTTP request.
 */
public class HttpHeader {

    private String key;

    private String[] values;

    public HttpHeader(final String key, final String... values) {
        this.key = key;
        this.values = values;
    }

    public String getKey() {
        return key;
    }

    public String[] getValues() {
        return values;
    }

}
