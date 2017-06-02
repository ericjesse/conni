package com.ericjesse.conni.http;

import com.ericjesse.conni.http.errors.ConnectionError;
import com.ericjesse.conni.http.errors.ConniError;
import com.ericjesse.conni.http.errors.UnexpectedError;
import com.ericjesse.conni.http.errors.UnknownHostError;
import com.ericjesse.conni.processors.ResponseObserver;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.GzipSink;
import okio.GzipSource;
import okio.Okio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * HttpClient is the default implementation of HttpConnectivityChecker to check the connectivity on the Internet.
 * This implementation is based upon OkHttp 3 and should be the only part in the code depending on the actual HTTP library.
 */
public class HttpClient implements HttpConnectivityChecker {

    // Default URL of the ping service.
    public static final String DEFAULT_SERVICE_URL = "https://ericjesse-whatsmyip.herokuapp.com/ip";

    public static final String CONTENT_ENCODING_HEADER_NAME = "Content-Encoding";

    public static final String CONTENT_ENCODING_VALUE_GZIP = "gzip";

    private static final Logger LOG = LoggerFactory.getLogger(HttpClient.class);

    private static final int DEFAULT_TIMEOUT_IN_MS = 2_000;

    private static final HttpRequest DEFAULT_REQUEST = new HttpRequest(DEFAULT_SERVICE_URL);

    private final OkHttpClient client;

    private final HttpRequest requestPrototype;

    private final Request actualRequest;

    private final List<ResponseObserver> observers = new LinkedList<>();

    private boolean initialized = false;

    public HttpClient() throws InvalidRequestException {
        this(DEFAULT_REQUEST, DEFAULT_TIMEOUT_IN_MS);
    }

    public HttpClient(final HttpRequest requestPrototype) throws InvalidRequestException {
        this(requestPrototype, DEFAULT_TIMEOUT_IN_MS);
    }

    public HttpClient(final HttpRequest requestPrototype, final int timeoutInMs) throws InvalidRequestException {
        this.requestPrototype = requestPrototype;
        client = new OkHttpClient.Builder().addNetworkInterceptor(new GzipRequestInterceptor())
                .addNetworkInterceptor(new GzipResponseInterceptor()).connectTimeout(timeoutInMs, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutInMs, TimeUnit.MILLISECONDS).writeTimeout(timeoutInMs, TimeUnit.MILLISECONDS)
                .build();
        // Convert the actualRequest.
        try {
            this.actualRequest = convertRequest(this.requestPrototype);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(e);
        }
    }

    // Visible for tests.
    private Request convertRequest(final HttpRequest request) {
        Request.Builder builder = new Request.Builder();
        // Method and body.
        if (HttpMethod.GET.equals(request.getMethod())) {
            builder.get();
        } else {
            builder.method(request.getMethod().name(),
                    RequestBody.create(MediaType.parse(request.getContentType().getValue()), request.getBody()));
        }
        // URL of the request.
        builder.url(request.getUrl());
        // Headers.
        request.getHeaders().forEach((key, values) -> values.forEach(value -> builder.addHeader(key, value)));

        return builder.build();
    }

    // Visible for test purpose only.
    OkHttpClient getClient() {
        return client;
    }

    // Visible for test purpose only.
    Request getActualRequest() {
        return actualRequest;
    }

    @Override
    public void addObserver(final ResponseObserver observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    @Override
    public void check() {
        if (!initialized) {
            Collections.sort(observers);
            initialized = true;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing request call to " + actualRequest.url().url().toExternalForm());
        }
        client.newCall(actualRequest).enqueue(new Callback() {

            @Override
            public void onFailure(final Call call, final IOException e) {
                ConniError error;
                if (e instanceof UnknownHostException) {
                    error = new UnknownHostError(call.request().url().host());
                } else if (e instanceof ConnectException) {
                    error = new ConnectionError();
                } else {
                    error = new UnexpectedError(e);
                }
                observers.forEach(o -> o.processError(error));
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                // Convert the response.
                final HttpResponse httpResponse = convertResponse(response);
                // Call the observers.
                observers.forEach(o -> o.processResponse(httpResponse));
            }
        });
    }

    private HttpResponse convertResponse(final Response response) throws IOException {
        return new HttpResponse(this.requestPrototype, response);
    }

    final class GzipRequestInterceptor implements Interceptor {

        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            final Request originalRequest = chain.request();
            if (originalRequest.body() == null || originalRequest.header(CONTENT_ENCODING_HEADER_NAME) != null) {
                return chain.proceed(originalRequest);
            }

            final Request compressedRequest =
                    originalRequest.newBuilder().header(CONTENT_ENCODING_HEADER_NAME, CONTENT_ENCODING_VALUE_GZIP)
                            .method(originalRequest.method(), gzip(originalRequest.body())).build();
            return chain.proceed(compressedRequest);
        }

        private RequestBody gzip(final RequestBody body) {
            return new RequestBody() {

                @Override
                public MediaType contentType() {
                    return body.contentType();
                }

                @Override
                public long contentLength() {
                    return -1; // We don't know the compressed length in advance!
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    final BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                    body.writeTo(gzipSink);
                    gzipSink.close();
                }
            };
        }
    }

    final class GzipResponseInterceptor implements Interceptor {

        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            final Response originalResponse = chain.proceed(chain.request());
            if (originalResponse.body() == null || originalResponse.header(CONTENT_ENCODING_HEADER_NAME) == null
                    || !originalResponse.header(CONTENT_ENCODING_HEADER_NAME).contains(CONTENT_ENCODING_VALUE_GZIP)) {
                return originalResponse;
            }

            return originalResponse.newBuilder().body(gunzip(originalResponse.body())).build();
        }

        private ResponseBody gunzip(final ResponseBody body) {
            return new ResponseBody() {

                @Override
                public MediaType contentType() {
                    return body.contentType();
                }

                @Override
                public long contentLength() {
                    return -1; // We don't know the uncompressed length in advance!
                }

                @Override
                public BufferedSource source() {
                    return Okio.buffer(new GzipSource(body.source()));
                }
            };
        }
    }
    // TODO Create a Logger Interceptor
}
