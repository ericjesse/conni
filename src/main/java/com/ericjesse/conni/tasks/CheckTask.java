package com.ericjesse.conni.tasks;

import com.ericjesse.conni.http.HttpClient;
import com.ericjesse.conni.http.HttpResponse;
import com.ericjesse.conni.http.errors.ConniError;
import com.ericjesse.conni.processors.ResponseObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default task to check the connectivity.
 */
public class CheckTask implements ResponseObserver, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(CheckTask.class);

    private final HttpClient httpClient;

    private boolean run = true;

    private int nextCallAfterSuccessInMs = 20_000;

    private int nextCallAfterFailureInMs = 5_000;

    private AtomicInteger waitingTimeInMs = new AtomicInteger(nextCallAfterSuccessInMs);

    public CheckTask(final HttpClient httpClient) {
        this.httpClient = httpClient;
        this.httpClient.addObserver(this);
    }

    @Override
    public ConniError processError(final ConniError error) {
        // Put a shorter time to check the connection.
        waitingTimeInMs.set(nextCallAfterFailureInMs);
        return error;
    }

    @Override
    public HttpResponse processResponse(final HttpResponse response) {
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 400) {
            waitingTimeInMs.set(nextCallAfterSuccessInMs);
        } else {
            waitingTimeInMs.set(nextCallAfterFailureInMs);
        }
        return response;
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void run() {
        while (run) {
            httpClient.check();
            try {
                Thread.sleep(waitingTimeInMs.get());
            } catch (InterruptedException e) {
                run = false;
                LOG.error(e.getMessage(), e);
                // Clean up state.
                Thread.currentThread().interrupt();
            }
        }
    }
}
