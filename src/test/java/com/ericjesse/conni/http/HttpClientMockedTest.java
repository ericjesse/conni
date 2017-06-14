package com.ericjesse.conni.http;

import com.ericjesse.conni.http.errors.ConnectionError;
import com.ericjesse.conni.http.errors.ConniError;
import com.ericjesse.conni.http.errors.UnknownHostError;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import okio.Buffer;
import org.apache.http.client.utils.URIBuilder;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class HttpClientMockedTest {

    static final String HOST = "localhost";

    static final int WEB_PORT = 10000;

    static final int TEST_TIMEOUT = 3000;

    static final int ACQUIRE_TIMEOUT = TEST_TIMEOUT + 100;

    private static final Logger LOG = LoggerFactory.getLogger(HttpClient.class);

    final MockWebServer webServer = new MockWebServer();

    @Before
    public void setUp() throws IOException {
        webServer.start(WEB_PORT);
    }

    @After
    public void tearDown() {
        try {
            webServer.shutdown();
        } catch (IOException e) {
            LOG.info(e.getMessage(), e);
        }
    }

    @Test(timeout = TEST_TIMEOUT)
    public void getOnValidPage() throws Exception {
        String url = webServer.url("/getOnValidPage").encodedPath();
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody("Herman Melville - Moby-Dick"));

        URI uri = new URIBuilder().setScheme("http").setHost(webServer.getHostName()).setPort(webServer.getPort())
                .setPath(url).build();

        HttpRequest request = new HttpRequest(uri.toASCIIString());
        HttpClient httpClient = new HttpClient(request);
        CaptorObserver obs = new CaptorObserver();
        httpClient.addObserver(obs);
        WaitingObserver wo = new WaitingObserver(1);
        httpClient.addObserver(wo);

        // Run the actual HTTP call.
        Instant before = Instant.now().minusMillis(1);
        httpClient.check();
        wo.waitGroup(ACQUIRE_TIMEOUT);
        obs.assertProcessedErrors(0);
        obs.assertProcessedResponses(1);

        // @formatter:off
        obs.assertNext(
            allOf(
                hasProperty("sendingRequestInstant", greaterThan(before)),
                hasProperty("receivedResponseInstant", greaterThan(before)),
                hasProperty("statusCode", is(200)),
                hasProperty("reasonPhrase", is("OK")),
                hasProperty("bodyContent", equalTo("Herman Melville - Moby-Dick"))
        ));
        // @formatter:on

        assertEquals(1, webServer.getRequestCount());
    }

    @Test(timeout = TEST_TIMEOUT)
    public void testRepeat() throws Exception {
        URI uri = new URIBuilder().setScheme("http").setHost(webServer.getHostName()).setPort(webServer.getPort())
                .setPath("/html").build();
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody("Herman Melville - Moby-Dick"));
        webServer.enqueue(new MockResponse().setResponseCode(204));

        HttpRequest request = new HttpRequest(uri.toASCIIString());
        HttpClient httpClient = new HttpClient(request);
        CaptorObserver obs = new CaptorObserver();
        httpClient.addObserver(obs);
        WaitingObserver wo = new WaitingObserver(2);
        httpClient.addObserver(wo);

        // Run the actual HTTP call twice.
        Instant before = Instant.now().minusMillis(1);
        httpClient.check();
        httpClient.check();
        wo.waitGroup(ACQUIRE_TIMEOUT);
        obs.assertProcessedErrors(0);
        obs.assertProcessedResponses(2);

        // @formatter:off
        obs.assertNext(
            allOf(
                hasProperty("sendingRequestInstant", greaterThan(before)),
                hasProperty("receivedResponseInstant", greaterThan(before)),
                hasProperty("statusCode", is(200)),
                hasProperty("reasonPhrase", is("OK")),
                hasProperty("bodyContent", containsString("Herman Melville - Moby-Dick"))
        ));
        obs.assertNext(
            allOf(
                hasProperty("sendingRequestInstant", greaterThan(before)),
                hasProperty("receivedResponseInstant", greaterThan(before)),
                hasProperty("statusCode", is(204)),
                hasProperty("reasonPhrase", is("OK")),
                hasProperty("bodyContent", is(""))
        ));
        // @formatter:on

        assertEquals(2, webServer.getRequestCount());
    }

    @Test(timeout = TEST_TIMEOUT)
    public void getOnInvalidHost() throws Exception {
        URI uri = new URIBuilder().setScheme("http").setHost("httpnobin.org").build();

        HttpRequest request = new HttpRequest(uri.toASCIIString());
        HttpClient httpClient = new HttpClient(request, 50);
        CaptorObserver obs = new CaptorObserver();
        httpClient.addObserver(obs);
        WaitingObserver wo = new WaitingObserver(1);
        httpClient.addObserver(wo);

        // Run the actual HTTP call.
        httpClient.check();
        wo.waitGroup(ACQUIRE_TIMEOUT);
        obs.assertProcessedErrors(1);
        obs.assertProcessedResponses(0);

        // @formatter:off
        obs.assertNext(
            allOf(
                instanceOf(UnknownHostError.class),
                hasProperty("hostname", is("httpnobin.org"))
        ));
        // @formatter:on

        assertEquals(0, webServer.getRequestCount());
    }

    @Test(timeout = TEST_TIMEOUT)
    public void getNotExistingPage() throws Exception {
        String url = webServer.url("/invalidpage").encodedPath();
        webServer.enqueue(new MockResponse().setResponseCode(404).setStatus("HTTP/1.1 404 Not Found"));

        URI uri = new URIBuilder().setScheme("http").setHost(webServer.getHostName()).setPort(webServer.getPort())
                .setPath(url).build();

        HttpRequest request = new HttpRequest(uri.toASCIIString());
        HttpClient httpClient = new HttpClient(request);
        CaptorObserver obs = new CaptorObserver();
        httpClient.addObserver(obs);
        WaitingObserver wo = new WaitingObserver(1);
        httpClient.addObserver(wo);

        // Run the actual HTTP call.
        Instant before = Instant.now().minusMillis(1);
        httpClient.check();
        wo.waitGroup(ACQUIRE_TIMEOUT);
        obs.assertProcessedErrors(0);
        obs.assertProcessedResponses(1);

        // @formatter:off
        obs.assertNext(
            allOf(
                hasProperty("sendingRequestInstant", greaterThan(before)),
                hasProperty("receivedResponseInstant", greaterThan(before)),
                hasProperty("statusCode", is(404)),
                hasProperty("reasonPhrase", is("Not Found")),
                hasProperty("bodyContent", is(""))
        ));
        // @formatter:on

        assertEquals(1, webServer.getRequestCount());
    }

    @Test(timeout = TEST_TIMEOUT)
    public void getOnAuthenticationNeeded() throws Exception {
        URI uri = new URIBuilder().setScheme("http").setHost(webServer.getHostName()).setPort(webServer.getPort())
                .setPath("/basic-auth/user/passwd").build();
        webServer.enqueue(new MockResponse().setResponseCode(401).setStatus("HTTP/1.1 401 Unauthorized"));

        HttpRequest request = new HttpRequest(uri.toASCIIString());
        HttpClient httpClient = new HttpClient(request);
        CaptorObserver obs = new CaptorObserver();
        httpClient.addObserver(obs);
        WaitingObserver wo = new WaitingObserver(1);
        httpClient.addObserver(wo);

        // Run the actual HTTP call.
        Instant before = Instant.now().minusMillis(1);
        httpClient.check();
        wo.waitGroup(ACQUIRE_TIMEOUT);
        obs.assertProcessedErrors(0);
        obs.assertProcessedResponses(1);

        // @formatter:off
        obs.assertNext(
            allOf(
                hasProperty("sendingRequestInstant", greaterThan(before)),
                hasProperty("receivedResponseInstant", greaterThan(before)),
                hasProperty("statusCode", is(401)),
                hasProperty("reasonPhrase", is("Unauthorized")),
                hasProperty("bodyContent", is(""))
        ));
        // @formatter:on

        assertEquals(1, webServer.getRequestCount());
    }

    @Test(timeout = TEST_TIMEOUT)
    public void getWithTimeout() throws Exception {
        URI uri = new URIBuilder().setScheme("http").setHost(webServer.getHostName()).setPort(webServer.getPort())
                .setPath("/html").build();

        webServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

        HttpRequest request = new HttpRequest(uri.toASCIIString());
        HttpClient httpClient = new HttpClient(request);
        CaptorObserver obs = new CaptorObserver();
        httpClient.addObserver(obs);
        WaitingObserver wo = new WaitingObserver(1);
        httpClient.addObserver(wo);

        // Run the actual HTTP call.
        httpClient.check();
        wo.waitGroup(ACQUIRE_TIMEOUT);
        obs.assertProcessedErrors(1);
        obs.assertProcessedResponses(0);

        // @formatter:off
        obs.assertNext(
            allOf(
                instanceOf(ConnectionError.class)
        ));
        // @formatter:on

        assertEquals(1, webServer.getRequestCount());
    }

    @Test(timeout = TEST_TIMEOUT)
    public void getOnWrongPort() throws Exception {
        URI uri = new URIBuilder().setScheme("http").setHost(webServer.getHostName()).setPort(webServer.getPort() + 1)
                .setPath("/html").build();

        HttpRequest request = new HttpRequest(uri.toASCIIString());
        HttpClient httpClient = new HttpClient(request);
        CaptorObserver obs = new CaptorObserver();
        httpClient.addObserver(obs);
        WaitingObserver wo = new WaitingObserver(1);
        httpClient.addObserver(wo);

        // Run the actual HTTP call.
        httpClient.check();
        wo.waitGroup(ACQUIRE_TIMEOUT);
        obs.assertProcessedErrors(1);
        obs.assertProcessedResponses(0);

        // @formatter:off
        obs.assertNext(
            allOf(
                instanceOf(ConnectionError.class)
        ));
        // @formatter:on

        assertEquals(0, webServer.getRequestCount());
    }

    @Test(timeout = TEST_TIMEOUT)
    public void getOnWrongScheme() throws Exception {
        URI uri = new URIBuilder().setScheme("https").setHost(webServer.getHostName()).setPort(webServer.getPort())
                .setPath("/html").build();

        HttpRequest request = new HttpRequest(uri.toASCIIString());
        HttpClient httpClient = new HttpClient(request);
        CaptorObserver obs = new CaptorObserver();
        httpClient.addObserver(obs);
        WaitingObserver wo = new WaitingObserver(1);
        httpClient.addObserver(wo);

        // Run the actual HTTP call.
        httpClient.check();
        wo.waitGroup(ACQUIRE_TIMEOUT);
        obs.assertProcessedErrors(1);
        obs.assertProcessedResponses(0);

        // @formatter:off
        obs.assertNext(
            allOf(
                instanceOf(ConnectionError.class)
        ));
        // @formatter:on

        assertEquals(0, webServer.getRequestCount());
    }

    @Test(timeout = TEST_TIMEOUT)
    public void movedPermanently() throws Exception {
        URI uri = new URIBuilder().setScheme("http").setHost(webServer.getHostName()).setPort(webServer.getPort())
                .setPath("/html").build();

        webServer.enqueue(new MockResponse().setResponseCode(301).setHeader("Location", "/other"));
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody("Herman Melville - Moby-Dick"));

        HttpRequest request = new HttpRequest(uri.toASCIIString());
        HttpClient httpClient = new HttpClient(request);
        CaptorObserver obs = new CaptorObserver();
        httpClient.addObserver(obs);
        WaitingObserver wo = new WaitingObserver(1);
        httpClient.addObserver(wo);

        // Run the actual HTTP call.
        Instant before = Instant.now().minusMillis(1);
        httpClient.check();
        wo.waitGroup(ACQUIRE_TIMEOUT);
        obs.assertProcessedErrors(0);
        obs.assertProcessedResponses(1);

        // @formatter:off
        obs.assertNext(
            allOf(
                hasProperty("sendingRequestInstant", greaterThan(before)),
                hasProperty("receivedResponseInstant", greaterThan(before)),
                hasProperty("statusCode", is(200)),
                hasProperty("reasonPhrase", is("OK")),
                hasProperty("bodyContent", containsString("Herman Melville - Moby-Dick"))
        ));
        // @formatter:on
        obs.assertProcessedErrors(0);
        obs.assertProcessedResponses(1);
        // The client should have followed the redirection.
        assertEquals(2, webServer.getRequestCount());
    }

    @Test(timeout = TEST_TIMEOUT)
    public void movedTemporarily() throws Exception {
        URI uri = new URIBuilder().setScheme("http").setHost(webServer.getHostName()).setPort(webServer.getPort())
                .setPath("/html").build();

        webServer.enqueue(new MockResponse().setResponseCode(302).setHeader("Location", "/other"));
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody("Herman Melville - Moby-Dick"));

        HttpRequest request = new HttpRequest(uri.toASCIIString());
        HttpClient httpClient = new HttpClient(request);
        CaptorObserver obs = new CaptorObserver();
        httpClient.addObserver(obs);
        WaitingObserver wo = new WaitingObserver(1);
        httpClient.addObserver(wo);

        // Run the actual HTTP call.
        Instant before = Instant.now().minusMillis(1);
        httpClient.check();
        wo.waitGroup(ACQUIRE_TIMEOUT);
        obs.assertProcessedErrors(0);
        obs.assertProcessedResponses(1);

        // @formatter:off
        obs.assertNext(
            allOf(
                hasProperty("sendingRequestInstant", greaterThan(before)),
                hasProperty("receivedResponseInstant", greaterThan(before)),
                hasProperty("statusCode", is(200)),
                hasProperty("reasonPhrase", is("OK")),
                hasProperty("bodyContent", containsString("Herman Melville - Moby-Dick"))
        ));
        // @formatter:on
        obs.assertProcessedErrors(0);
        obs.assertProcessedResponses(1);

        // The client should have followed the redirection.
        assertEquals(2, webServer.getRequestCount());
    }

    @Test(timeout = TEST_TIMEOUT)
    public void withGZipContent() throws Exception {
        URI uri = new URIBuilder().setScheme("http").setHost(webServer.getHostName()).setPort(webServer.getPort())
                .setPath("/html").build();

        // Write a GZIP content in the body.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(bos)));
        bw.write("Herman Melville - Moby-Dick");
        bw.flush();
        bw.close();

        Buffer bodyBuffer = new Buffer();
        bodyBuffer.write(bos.toByteArray());
        bodyBuffer.flush();
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(bodyBuffer)
                .setHeader("Content-Encoding", "gzip,deflate"));

        HttpRequest request = new HttpRequest(uri.toASCIIString());
        HttpClient httpClient = new HttpClient(request);
        CaptorObserver obs = new CaptorObserver();
        httpClient.addObserver(obs);
        WaitingObserver wo = new WaitingObserver(1);
        httpClient.addObserver(wo);

        // Run the actual HTTP call.
        Instant before = Instant.now().minusMillis(1);
        httpClient.check();
        wo.waitGroup(ACQUIRE_TIMEOUT);
        obs.assertProcessedErrors(0);
        obs.assertProcessedResponses(1);

        // @formatter:off
        obs.assertNext(
            allOf(
                hasProperty("sendingRequestInstant", greaterThan(before)),
                hasProperty("receivedResponseInstant", greaterThan(before)),
                hasProperty("headers", allOf(
                        hasEntry(is("content-encoding"), hasItem("gzip,deflate")),
                        hasEntry(is("content-length"),hasItem(Integer.toString(bos.toByteArray().length)))
                )),
                hasProperty("statusCode", is(200)),
                hasProperty("reasonPhrase", is("OK")),
                hasProperty("bodyContent", containsString("Herman Melville - Moby-Dick"))
        ));
        // @formatter:on
        assertEquals(1, webServer.getRequestCount());
    }

    /**
     * CaptorObserver is an observer to capture objects emitted by the HTTP client.
     */
    private class CaptorObserver implements com.ericjesse.conni.processors.ResponseObserver {

        private AtomicInteger processedResponses = new AtomicInteger();

        private AtomicInteger processedErrors = new AtomicInteger();

        private List<Object> receivedObject = new LinkedList<>();

        @Override
        public ConniError processError(final ConniError error) {
            processedErrors.incrementAndGet();
            receivedObject.add(error);
            return error;
        }

        @Override
        public HttpResponse processResponse(final HttpResponse response) {
            processedResponses.incrementAndGet();
            receivedObject.add(response);
            return response;
        }

        public void assertNext(final Matcher<Object> matchers) {
            final Object o = receivedObject.remove(0);
            assertThat(o, matchers);
        }

        public void assertProcessedResponses(final int expectedProcessedResponses) {
            assertEquals(expectedProcessedResponses, processedResponses.intValue());
        }

        public void assertProcessedErrors(final int expectedProcessedErrors) {
            assertEquals(expectedProcessedErrors, processedErrors.intValue());
        }

    }

    /**
     * WaitingObserver is an observer designed to block the current thread while the number of expected responses or errors were not processed.
     * <p>
     * This observer must be executed at the latest.
     */
    private class WaitingObserver implements com.ericjesse.conni.processors.ResponseObserver {

        private final int permits;

        private final Semaphore semaphore;

        public WaitingObserver(final int permits) throws InterruptedException {
            this.permits = permits;
            semaphore = new Semaphore(this.permits);
            semaphore.acquire(permits);
        }

        @Override
        public ConniError processError(final ConniError error) {
            semaphore.release();
            return error;
        }

        @Override
        public HttpResponse processResponse(final HttpResponse response) {
            semaphore.release();
            return response;
        }

        public void waitGroup(final int timeoutInMs) throws InterruptedException {
            semaphore.tryAcquire(this.permits, timeoutInMs, TimeUnit.MILLISECONDS);
        }

        @Override
        public int getOrder() {
            return Integer.MAX_VALUE;
        }
    }
}
