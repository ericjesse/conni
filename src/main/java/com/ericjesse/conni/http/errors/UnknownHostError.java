package com.ericjesse.conni.http.errors;

/**
 * Error raised when the host name cannot be resolved.
 */
public class UnknownHostError implements ConniError {

    private String hostname;

    public UnknownHostError(final String hostname) {
        this.hostname = hostname;
    }

    public String getHostname() {
        return hostname;
    }
}
