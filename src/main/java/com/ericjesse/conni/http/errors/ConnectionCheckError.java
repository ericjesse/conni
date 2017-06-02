package com.ericjesse.conni.http.errors;

/**
 * Exception representing a problem while reaching a remote address.
 */
public class ConnectionCheckError implements ConniError {

    private String message;

    public ConnectionCheckError() {
    }

    public ConnectionCheckError(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
