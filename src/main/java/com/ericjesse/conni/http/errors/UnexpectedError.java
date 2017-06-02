package com.ericjesse.conni.http.errors;

/**
 * Error representing an unexpected error.
 */
public class UnexpectedError implements ConniError {

    private Exception exception;

    public UnexpectedError(final Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
