package com.ericjesse.conni.http;

import okhttp3.Request;
import okio.Buffer;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Test class to validate the preparation of the {@link HttpClient}.
 */
public class HttpClientPreparationTest {

    @Test
    public void defaultClient() throws InvalidRequestException {
        HttpClient client = new HttpClient();

        assertEquals("The default connection timeout should be 2000 ms", client.getClient().connectTimeoutMillis(),
                2_000);

        Request convertedRequest = client.getActualRequest();
        assertNull("The body should be null by default", convertedRequest.body());
        assertEquals("The method should be GET by default", "GET", convertedRequest.method());
        assertEquals("The url should be https://ericjesse-whatsmyip.herokuapp.com/ip by default",
                "https://ericjesse-whatsmyip.herokuapp.com/ip", convertedRequest.url().url().toExternalForm());
        assertTrue("The scheme should be https by default", convertedRequest.isHttps());

        //@formatter:off
        assertThat("The HTTP header User-Agent should be Conni (com.ericjesse.conni) by default",
                convertedRequest.headers("User-Agent"),
                allOf(
                        Matchers.<String>hasSize(1),
                        Matchers.<String>hasItem("Conni (com.ericjesse.conni)")
                )
        );
        assertThat("The HTTP header Accept-Encoding should be gzip by default",
                convertedRequest.headers("Accept-Encoding"),
                allOf(
                        Matchers.<String>hasSize(1),
                        Matchers.<String>hasItem("gzip")
                )
        );
        assertThat("The HTTP header Content-Type should be application/json; charset=utf-8 by default",
                convertedRequest.headers("Content-Type"),
                allOf(
                        Matchers.<String>hasSize(1),
                        Matchers.<String>hasItem("application/json; charset=utf-8")
                )
        );
        assertThat("The HTTP header Accept-Content-Type should be application/json; charset=utf-8 by default",
                convertedRequest.headers("Accept-Content-Type"),
                allOf(
                        Matchers.<String>hasSize(1),
                        Matchers.<String>hasItem("application/json; charset=utf-8")
                )
        );
        //@formatter:on
    }

    @Test
    public void defaultClientWithDifferentTimeout() throws InvalidRequestException {
        HttpRequest request = new HttpRequest(HttpClient.DEFAULT_SERVICE_URL);
        HttpClient client = new HttpClient(request, 10_000);

        assertEquals("The expected connection timeout is 10000 ms", client.getClient().connectTimeoutMillis(), 10_000);
        assertNotNull(client.getActualRequest());
    }

    @Test
    public void convertGetRequestWithHeaders() throws InvalidRequestException {
        HttpRequest request = new HttpRequest("http://myUrl?foo=bar",
                // Header created with two instances of HttpHeader.
                new HttpHeader("head1", "value1.1"), new HttpHeader("head1", "value1.2"),
                new HttpHeader("head2", "value2"),
                // Header created with two values at once.
                new HttpHeader("head3", "value3.1", "value3.2"));

        HttpClient client = new HttpClient(request, 0);

        Request convertedRequest = client.getActualRequest();
        assertNull("The body should be null", convertedRequest.body());
        assertEquals("The method should be GET", "GET", convertedRequest.method());
        assertEquals("The url should be http://myurl?/foo=bar", "http://myurl/?foo=bar",
                convertedRequest.url().url().toExternalForm());
        assertFalse("The scheme should be http", convertedRequest.isHttps());

        //@formatter:off
        assertThat("The HTTP header User-Agent should be Conni (com.ericjesse.conni)",
                convertedRequest.headers("User-Agent"),
                allOf(
                        Matchers.<String>hasSize(1),
                        Matchers.<String>hasItem("Conni (com.ericjesse.conni)")
                )
        );
        assertThat("The HTTP header Accept-Encoding should be gzip",
                convertedRequest.headers("Accept-Encoding"),
                allOf(
                        Matchers.<String>hasSize(1),
                        Matchers.<String>hasItem("gzip")
                )
        );
        assertThat("The HTTP header Content-Type should be application/json; charset=utf-8",
                convertedRequest.headers("Content-Type"),
                allOf(
                        Matchers.<String>hasSize(1),
                        Matchers.<String>hasItem("application/json; charset=utf-8")
                )
        );
        assertThat("The HTTP header Accept-Content-Type should be application/json; charset=utf-8",
                convertedRequest.headers("Accept-Content-Type"),
                allOf(
                        Matchers.<String>hasSize(1),
                        Matchers.<String>hasItem("application/json; charset=utf-8")
                )
        );
        assertThat("The HTTP header head1 should be value1.1 and value1.1",
                convertedRequest.headers("head1"),
                allOf(
                        Matchers.<String>hasSize(2),
                        Matchers.<String>contains(
                                is("value1.1"),
                                is("value1.2")
                        )
                )
        );
        assertThat("The HTTP header head2 should be value2",
                convertedRequest.headers("head2"),
                allOf(
                        Matchers.<String>hasSize(1),
                        Matchers.<String>hasItem("value2")
                )
        );
        assertThat("The HTTP header head3 should be value3.1 and value3.1",
                convertedRequest.headers("head3"),
                allOf(
                        Matchers.<String>hasSize(2),
                        Matchers.<String>contains(
                                is("value3.1"),
                                is("value3.2")
                        )
                )
        );
        //@formatter:on
    }

    @Test
    public void convertPostXml() throws IOException, InvalidRequestException {
        HttpRequest request = new HttpRequest(HttpMethod.POST, "https://aUrlForXml/test.dot?foo=bar",
                "<content>Content of the body</content>", HttpContentType.XML,
                new HttpHeader("headXml1", "valueXml1.1"), new HttpHeader("headXml1", "valueXml1.2"),
                new HttpHeader("headXml2", "valueXml2"));

        HttpClient client = new HttpClient(request, 0);

        Request convertedRequest = client.getActualRequest();
        assertNotNull("The body should not be null", convertedRequest.body());
        Buffer buffer = new Buffer();
        convertedRequest.body().writeTo(buffer);
        assertEquals("The body should be <content>Content of the body</content>",
                "<content>Content of the body</content>", buffer.readUtf8());
        assertEquals("The method should be POST", "POST", convertedRequest.method());
        assertEquals("The url should be https://aurlforxml/test.dot?foo=bar", "https://aurlforxml/test.dot?foo=bar",
                convertedRequest.url().url().toExternalForm());
        assertTrue("The scheme should be https", convertedRequest.isHttps());

        //@formatter:off
        assertThat("The HTTP header User-Agent should be Conni (com.ericjesse.conni)",
                convertedRequest.headers("User-Agent"),
                allOf(
                        Matchers.<String>hasSize(1),
                        Matchers.<String>hasItem("Conni (com.ericjesse.conni)")
                )
        );
        assertThat("The HTTP header Accept-Encoding should be gzip",
                convertedRequest.headers("Accept-Encoding"),
                allOf(
                        Matchers.<String>hasSize(1),
                        Matchers.<String>hasItem("gzip")
                )
        );
        assertThat("The HTTP header Content-Type should be text/xml; charset=utf-8",
                convertedRequest.headers("Content-Type"),
                allOf(
                        Matchers.<String>hasSize(1),
                        Matchers.<String>hasItem("text/xml; charset=utf-8")
                )
        );
        assertThat("The HTTP header Accept-Content-Type should be text/xml; charset=utf-8",
                convertedRequest.headers("Accept-Content-Type"),
                allOf(
                        Matchers.<String>hasSize(1),
                        Matchers.<String>hasItem("text/xml; charset=utf-8")
                )
        );
        assertThat("The HTTP header headXml1 should be valueXml1.1 and valueXml1.1",
                convertedRequest.headers("headXml1"),
                allOf(
                        Matchers.<String>hasSize(2),
                        Matchers.<String>contains(
                                is("valueXml1.1"),
                                is("valueXml1.2")
                        )
                )
        );
        assertThat("The HTTP header headXml2 should be valueXml2",
                convertedRequest.headers("headXml2"),
                allOf(
                        Matchers.<String>hasSize(1),
                        Matchers.<String>hasItem("valueXml2")
                )
        );
        //@formatter:on
    }

    @Test
    public void convertPutHtml() throws IOException, InvalidRequestException {
        HttpRequest request = new HttpRequest(HttpMethod.PUT, "http://aUrlForHtml/test.dot?foo=bar",
                "<html>Content of the body</html>", HttpContentType.HTML, new HttpHeader("headHtml1", "valueHtml1.1"),
                new HttpHeader("headHtml1", "valueHtml1.2"), new HttpHeader("headHtml2", "valueHtml2"));

        HttpClient client = new HttpClient(request, 0);

        Request convertedRequest = client.getActualRequest();
        assertNotNull("The body should not be null", convertedRequest.body());
        Buffer buffer = new Buffer();
        convertedRequest.body().writeTo(buffer);
        assertEquals("The body should be <html>Content of the body</html>", "<html>Content of the body</html>",
                buffer.readUtf8());
        assertEquals("The method should be PUT", "PUT", convertedRequest.method());
        assertEquals("The url should be http://aurlforhtml/test.dot?foo=bar", "http://aurlforhtml/test.dot?foo=bar",
                convertedRequest.url().url().toExternalForm());
        assertFalse("The scheme should be http", convertedRequest.isHttps());

        //@formatter:off
        assertThat("The HTTP header User-Agent should be Conni (com.ericjesse.conni)",
                convertedRequest.headers("User-Agent"),
                allOf(
                        Matchers.<String>hasSize(1),
                        Matchers.<String>hasItem("Conni (com.ericjesse.conni)")
                )
        );
        assertThat("The HTTP header Accept-Encoding should be gzip",
                convertedRequest.headers("Accept-Encoding"),
                allOf(
                        Matchers.<String>hasSize(1),
                        Matchers.<String>hasItem("gzip")
                )
        );
        assertThat("The HTTP header Content-Type should be text/html; charset=utf-8",
                convertedRequest.headers("Content-Type"),
                allOf(
                        Matchers.<String>hasSize(1),
                        Matchers.<String>hasItem("text/html; charset=utf-8")
                )
        );
        assertThat("The HTTP header Accept-Content-Type should be text/html; charset=utf-8",
                convertedRequest.headers("Accept-Content-Type"),
                allOf(
                        Matchers.<String>hasSize(1),
                        Matchers.<String>hasItem("text/html; charset=utf-8")
                )
        );
        assertThat("The HTTP header headHtml1 should be valueHtml1.1 and valueHtml1.1",
                convertedRequest.headers("headHtml1"),
                allOf(
                        Matchers.<String>hasSize(2),
                        Matchers.<String>contains(
                                is("valueHtml1.1"),
                                is("valueHtml1.2")
                        )
                )
        );
        assertThat("The HTTP header headHtml2 should be valueHtml2",
                convertedRequest.headers("headHtml2"),
                allOf(
                        Matchers.<String>hasSize(1),
                        Matchers.<String>hasItem("valueHtml2")
                )
        );
        //@formatter:on
    }

    @Test(expected = InvalidRequestException.class)
    public void getOnInvalidScheme() throws Exception {

        // Wrong scheme.
        HttpRequest request = new HttpRequest("httm://hostname");
        new HttpClient(request, 50);
    }

    @Test(expected = InvalidRequestException.class)
    public void getOnInvalidUrl() throws Exception {

        // Wrong URL.
        HttpRequest request = new HttpRequest("httm_hostname");
        new HttpClient(request, 50);
    }
}
