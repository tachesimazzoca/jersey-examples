package app.core;

import static org.junit.Assert.*;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.UriBuilder;

import com.google.common.collect.ImmutableMap;

import static app.core.Util.*;

public class UtilTest {
    @Test
    public void testParams() {
        Map<String, Object> m = params("foo", "bar", "baz", 1234);
        assertEquals(ImmutableMap.<String, Object> of("foo", "bar", "baz", 1234), m);
    }

    @Test
    public void testBase64() throws IOException, ClassNotFoundException {
        String s = "deadbeef";
        String ser = objectToBase64(s);
        assertEquals(s, base64ToObject(ser, String.class));

        Map<String, Object> m = ImmutableMap.of("foo", (Object) 1234, "bar", "baz");
        ser = objectToBase64(m);
        Map<?, ?> n = base64ToObject(ser, Map.class);
        assertEquals(m, n);
        assertEquals(1234, n.get("foo"));
        assertEquals("baz", n.get("bar"));
    }

    private UriInfo mockUriInfo(String path) {
        UriInfo uinfo = mock(UriInfo.class);
        UriBuilder baseUriBuilder = UriBuilder.fromPath(path);
        when(uinfo.getBaseUriBuilder()).thenReturn(baseUriBuilder);
        return uinfo;
    }

    @Test
    public void testSafeURI() {
        UriInfo uinfo;

        uinfo = mockUriInfo("http://localhost/");
        assertEquals("http://localhost/dashboard?foo=bar",
                safeURI(uinfo, "/dashboard?foo=bar").toString());

        uinfo = mockUriInfo("http://localhost/a/");
        assertEquals("http://localhost/a/dashboard?foo=bar",
                safeURI(uinfo, "/dashboard?foo=bar").toString());

        uinfo = mockUriInfo("http://localhost/a/");
        assertEquals("http://localhost/a/dashboard?foo=bar",
                safeURI(uinfo, "./dashboard?foo=bar").toString());

        uinfo = mockUriInfo("http://localhost/a/");
        assertEquals("http://localhost/dashboard?foo=bar",
                safeURI(uinfo, "../dashboard?foo=bar").toString());

        uinfo = mockUriInfo("http://localhost/");
        assertEquals("http://www.example.net/bar/",
                safeURI(uinfo, "http://www.example.net/bar/").toString());

        uinfo = mockUriInfo("http://localhost/");
        assertEquals("https://ssl.example.net/bar/",
                safeURI(uinfo, "https://ssl.example.net/bar/").toString());

        uinfo = mockUriInfo("http://localhost/a/");
        assertEquals("http://localhost/a/http.html",
                safeURI(uinfo, "http.html").toString());

        uinfo = mockUriInfo("http://localhost/b/");
        assertEquals("http://localhost/b/https.html",
                safeURI(uinfo, "https.html").toString());
    }
}
