package app.core;

import static org.junit.Assert.*;
import org.junit.Test;

import javax.ws.rs.core.NewCookie;

import com.google.common.base.Optional;

public class CookieBakerTest {
    @Test
    public void testEmptyCookie() {
        NewCookie emptyCookie = new NewCookie(
                "TEST", "", "/test", "localhost",
                NewCookie.DEFAULT_VERSION, "HTTPOnly",
                -86400, true);
        CookieBaker baker = new CookieBaker(emptyCookie);
        assertEquals(Optional.<String> absent(), baker.get("nokey"));
        assertEquals(emptyCookie, baker.toCookie());
    }

    @Test
    public void testInvalidCookie() {
        CookieBaker baker;
        baker = new CookieBaker(new NewCookie("TEST", "dead"));
        assertTrue(baker.isEmpty());
        baker = new CookieBaker(new NewCookie("TEST", "foo="));
        assertTrue(baker.isEmpty());
        baker = new CookieBaker(new NewCookie("TEST", "=bar"));
        assertTrue(baker.isEmpty());
        baker = new CookieBaker(new NewCookie("TEST", "&foo=bar"));
        assertTrue(baker.isEmpty());
        baker = new CookieBaker(new NewCookie("TEST", "foo=bar&"));
        assertTrue(baker.isEmpty());
        baker = new CookieBaker(new NewCookie("TEST", "foo=bar&&baz=qux"));
        assertTrue(baker.isEmpty());
        baker = new CookieBaker(new NewCookie("TEST", "foo=bar&baz=qux"));
        assertFalse(baker.isEmpty());
    }

    @Test
    public void testPutAndGet() {
        CookieBaker baker = new CookieBaker(new NewCookie(
                "TEST", "", "/", "",
                NewCookie.DEFAULT_VERSION, "",
                NewCookie.DEFAULT_MAX_AGE, false));
        assertEquals(Optional.<String> absent(), baker.get("nokey"));
        baker.put("id", "1234");
        String ts = new Long(System.currentTimeMillis()).toString();
        baker.put("timestamp", ts);
        assertEquals("1234", baker.get("id").get());
        assertEquals(ts, baker.get("timestamp").get());
    }
}
