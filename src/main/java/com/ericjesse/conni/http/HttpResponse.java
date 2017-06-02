package com.ericjesse.conni.http;

import okhttp3.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Response of a HTTP call.
 */
public class HttpResponse {

    private final Instant sendingRequestInstant;

    private final Instant receivedResponseInstant;

    private final Duration duration;

    private HttpRequest request;

    private int statusCode;

    private String reasonPhrase;

    private String bodyContent;

    private Map<String, List<String>> headers = new HashMap<>();

    public HttpResponse(final HttpRequest request, final Response response) throws IOException {
        assert null != response;
        this.request = request;
        sendingRequestInstant = Instant.ofEpochMilli(response.sentRequestAtMillis());
        receivedResponseInstant = Instant.ofEpochMilli(response.receivedResponseAtMillis());
        duration = Duration.between(sendingRequestInstant, receivedResponseInstant);
        statusCode = response.code();
        reasonPhrase = response.message();
        bodyContent = response.body().string();
        headers.putAll(response.headers().toMultimap());
    }

    public HttpRequest getRequest() {
        return request;
    }

    public Instant getSendingRequestInstant() {
        return sendingRequestInstant;
    }

    public Instant getReceivedResponseInstant() {
        return receivedResponseInstant;
    }

    public Duration getDuration() {
        return duration;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public InputStream getBodyContentAsStream() {
        return new ByteArrayInputStream(bodyContent.getBytes());
    }
}
