package app.core.util;

import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class URIUtilsTest {
    private UriInfo mockUriInfo(String path) {
        UriInfo uriInfo = mock(UriInfo.class);
        UriBuilder baseUriBuilder = UriBuilder.fromPath(path);
        when(uriInfo.getBaseUriBuilder()).thenReturn(baseUriBuilder);
        return uriInfo;
    }

    @Test
    public void testSafeURI() {
        UriInfo uriInfo;

        uriInfo = mockUriInfo("http://localhost/");
        assertEquals("http://localhost/dashboard?foo=bar",
                URIUtils.safeURI(uriInfo, "/dashboard?foo=bar").toString());

        uriInfo = mockUriInfo("http://localhost/a/");
        assertEquals("http://localhost/a/dashboard?foo=bar",
                URIUtils.safeURI(uriInfo, "/dashboard?foo=bar").toString());

        uriInfo = mockUriInfo("http://localhost/a/");
        assertEquals("http://localhost/a/dashboard?foo=bar",
                URIUtils.safeURI(uriInfo, "./dashboard?foo=bar").toString());

        uriInfo = mockUriInfo("http://localhost/a/");
        assertEquals("http://localhost/dashboard?foo=bar",
                URIUtils.safeURI(uriInfo, "../dashboard?foo=bar").toString());

        uriInfo = mockUriInfo("http://localhost/");
        assertEquals("http://www.example.net/bar/",
                URIUtils.safeURI(uriInfo, "http://www.example.net/bar/").toString());

        uriInfo = mockUriInfo("http://localhost/");
        assertEquals("https://ssl.example.net/bar/",
                URIUtils.safeURI(uriInfo, "https://ssl.example.net/bar/").toString());

        uriInfo = mockUriInfo("http://localhost/a/");
        assertEquals("http://localhost/a/http.html",
                URIUtils.safeURI(uriInfo, "http.html").toString());

        uriInfo = mockUriInfo("http://localhost/b/");
        assertEquals("http://localhost/b/https.html",
                URIUtils.safeURI(uriInfo, "https.html").toString());
    }
}
