package com.ericjesse.conni.http;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Test class to validate the preparation of the {@link HttpRequest}.
 */
public class HttpRequestTest {

    @Test
    public void defaultGet() {
        HttpRequest request = new HttpRequest("myUrl");

        //@formatter:off
        assertThat(request, allOf(
                hasProperty("method", is(HttpMethod.GET)),
                hasProperty("url", is("myUrl")),
                hasProperty("body", nullValue()),
                hasProperty("contentType", is(HttpContentType.JSON)),
                hasProperty("headers", allOf(
                        hasEntry(is("User-Agent"), hasItem("Conni (com.ericjesse.conni)")),
                        hasEntry(is("Accept-Encoding"), hasItem("gzip")),
                        hasEntry(is("Content-Type"), hasItem("application/json; charset=utf-8")),
                        hasEntry(is("Accept-Content-Type"), hasItem("application/json; charset=utf-8"))
                    )
                )
        ));
        //@formatter:on
    }

    @Test
    public void simpleGetWithHeaders() {
        HttpRequest request = new HttpRequest("myOtherUrl",
                // Header created with two instances of HttpHeader.
                new HttpHeader("head1", "value1.1"), new HttpHeader("head1", "value1.2"),
                new HttpHeader("head2", "value2"),
                // Header created with two values at once.
                new HttpHeader("head3", "value3.1", "value3.2"));

        //@formatter:off
        assertThat(request, allOf(
                hasProperty("method", is(HttpMethod.GET)),
                hasProperty("url", is("myOtherUrl")),
                hasProperty("body", nullValue()),
                hasProperty("contentType", is(HttpContentType.JSON)),
                hasProperty("headers", Matchers.<Map<String, List<String>>>allOf(
                        hasEntry(is("User-Agent"), hasItem("Conni (com.ericjesse.conni)")),
                        hasEntry(is("Accept-Encoding"), hasItem("gzip")),
                        hasEntry(is("Content-Type"), hasItem("application/json; charset=utf-8")),
                        hasEntry(is("Accept-Content-Type"), hasItem("application/json; charset=utf-8")),
                        hasEntry(is("head1"), contains(
                                is("value1.1"),
                                is("value1.2")
                        )),
                        hasEntry(is("head2"), hasItem("value2")),
                        hasEntry(is("head3"), contains(
                                is("value3.1"),
                                is("value3.2")
                        ))
                    )
                )
        ));
        //@formatter:on
    }

    @Test
    public void postXml() {
        HttpRequest request = new HttpRequest(HttpMethod.POST, "aUrlForXml", "<content>Content of the body</content>",
                HttpContentType.XML, new HttpHeader("headXml1", "valueXml1.1"),
                new HttpHeader("headXml1", "valueXml1.2"), new HttpHeader("headXml2", "valueXml2"));

        //@formatter:off
        assertThat(request, allOf(
                hasProperty("method", is(HttpMethod.POST)),
                hasProperty("url", is("aUrlForXml")),
                hasProperty("body", is("<content>Content of the body</content>")),
                hasProperty("contentType", is(HttpContentType.XML)),
                hasProperty("headers", Matchers.<Map<String, List<String>>>allOf(
                        hasEntry(is("User-Agent"), hasItem("Conni (com.ericjesse.conni)")),
                        hasEntry(is("Accept-Encoding"), hasItem("gzip")),
                        hasEntry(is("Content-Type"), hasItem("text/xml; charset=utf-8")),
                        hasEntry(is("Accept-Content-Type"), hasItem("text/xml; charset=utf-8")),
                        hasEntry(is("headXml1"), contains(
                                is("valueXml1.1"),
                                is("valueXml1.2")
                        )),
                        hasEntry(is("headXml2"), hasItem("valueXml2"))
                    )
                )
        ));
        //@formatter:on
    }

    @Test
    public void putHtml() {
        HttpRequest request =
                new HttpRequest(HttpMethod.PUT, "aUrlForHtml", "<html>Content of the body</html>", HttpContentType.HTML,
                        new HttpHeader("headHtml1", "valueHtml1.1"), new HttpHeader("headHtml1", "valueHtml1.2"),
                        new HttpHeader("headHtml2", "valueHtml2"));

        //@formatter:off
        assertThat(request, allOf(
                hasProperty("method", is(HttpMethod.PUT)),
                hasProperty("url", is("aUrlForHtml")),
                hasProperty("body", is("<html>Content of the body</html>")),
                hasProperty("contentType", is(HttpContentType.HTML)),
                hasProperty("headers", Matchers.<Map<String, List<String>>>allOf(
                        hasEntry(is("User-Agent"), hasItem("Conni (com.ericjesse.conni)")),
                        hasEntry(is("Accept-Encoding"), hasItem("gzip")),
                        hasEntry(is("Content-Type"), hasItem("text/html; charset=utf-8")),
                        hasEntry(is("Accept-Content-Type"), hasItem("text/html; charset=utf-8")),
                        hasEntry(is("headHtml1"), contains(
                                is("valueHtml1.1"),
                                is("valueHtml1.2")
                        )),
                        hasEntry(is("headHtml2"), hasItem("valueHtml2"))
                    )
                )
        ));
        //@formatter:on
    }

    @Test
    public void overwriteDefaultHeaders() {
        HttpRequest request = new HttpRequest("myDifferentUrl", new HttpHeader("User-Agent", "myUser-Agent"),
                new HttpHeader("Accept-Encoding", "deflate"));

        //@formatter:off
        assertThat(request, allOf(
                hasProperty("method", is(HttpMethod.GET)),
                hasProperty("url", is("myDifferentUrl")),
                hasProperty("body", nullValue()),
                hasProperty("contentType", is(HttpContentType.JSON)),
                hasProperty("headers", Matchers.<Map<String, List<String>>>allOf(
                        hasEntry(is("User-Agent"), hasItem("myUser-Agent")),
                        hasEntry(is("Accept-Encoding"), hasItem("deflate")),
                        hasEntry(is("Content-Type"), hasItem("application/json; charset=utf-8")),
                        hasEntry(is("Accept-Content-Type"), hasItem("application/json; charset=utf-8"))
                    )
                )
        ));
        //@formatter:on
    }
}
