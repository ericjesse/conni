package com.ericjesse.conni.http.errors;

/**
 * Error raised when the HTTP URL is not valid.
 */
public class InvalidUrlError implements ConniError {

    private String url;

    public InvalidUrlError(final String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
