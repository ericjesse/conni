package com.ericjesse.conni.http;

import com.ericjesse.conni.processors.ResponseObserver;

/**
 * HttpConnectivityChecker is an interface to implement by any class dealing with HTTP requests to check the connectivity.
 */
public interface HttpConnectivityChecker {

    void addObserver(ResponseObserver observer);

    void check() throws ConniException;
}
