package com.ericjesse.conni.processors;

import com.ericjesse.conni.http.HttpResponse;
import com.ericjesse.conni.http.errors.ConniError;

/**
 * ResponseObserver is an interface to implement for all the classes dealing with the HTTP requests or responses.
 */
public interface ResponseObserver extends Comparable<ResponseObserver> {

    /**
     * processError is teh method called by the observed when a HTTP call resulted in an expected error.
     *
     * @param error the response passed by the HttpConnectivityChecker or the previously called {@link ResponseObserver}.
     * @return the passed error or a totally different one.
     */
    ConniError processError(ConniError error);

    /**
     * processResponse is the method called by the observed when a response object was built. The concrete
     * implementation can also return a totally different response.
     *
     * @param response the response passed by the HttpConnectivityChecker or the previously called {@link ResponseObserver}.
     * @return the passed response or a totally different one.
     */
    HttpResponse processResponse(HttpResponse response);

    /**
     * Order of execution of the observer in the chain. Low values are performed first.
     */
    default int getOrder() {
        return 0;
    }

    @Override
    default int compareTo(ResponseObserver o) {
        return getOrder() - o.getOrder();
    }
}
