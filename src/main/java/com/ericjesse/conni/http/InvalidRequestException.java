package com.ericjesse.conni.http;

/**
 * Error raised when the HTTP URL is not valid.
 */
public class InvalidRequestException extends Exception {

    public InvalidRequestException(final Throwable cause) {
        super(cause);
    }
}
