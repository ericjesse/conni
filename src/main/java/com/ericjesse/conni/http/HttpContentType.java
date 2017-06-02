package com.ericjesse.conni.http;

/**
 * Enum representing the most common HTTP content types used by the app.
 */
public enum HttpContentType {

    JSON("application/json; charset=utf-8"), HTML("text/html; charset=utf-8"), XML("text/xml; charset=utf-8");

    private String value;

    HttpContentType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
