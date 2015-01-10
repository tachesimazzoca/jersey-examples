package app.core.session;

import com.google.common.base.Optional;
import org.junit.Test;

import javax.ws.rs.core.NewCookie;

import static org.junit.Assert.*;

public class CookieSessionTest {
    @Test
    public void testEmptyCookie() {
        NewCookie emptyCookie = new NewCookie(
                "TEST", "", "/test", "localhost",
                NewCookie.DEFAULT_VERSION, "HTTPOnly",
                -86400, true);
        CookieSession session = new CookieSession(emptyCookie, null);
        assertEquals(Optional.<String>absent(), session.get("nokey"));
        assertEquals(emptyCookie, session.toCookie());
    }

    @Test
    public void testInvalidCookie() {
        Optional<String> secret = Optional.absent();
        CookieSession session;
        session = new CookieSession(new NewCookie("TEST", "dead"));
        assertTrue(session.isEmpty());
        session = new CookieSession(new NewCookie("TEST", "foo="));
        assertTrue(session.isEmpty());
        session = new CookieSession(new NewCookie("TEST", "=bar"));
        assertTrue(session.isEmpty());
        session = new CookieSession(new NewCookie("TEST", "&foo=bar"));
        assertTrue(session.isEmpty());
        session = new CookieSession(new NewCookie("TEST", "foo=bar&"));
        assertTrue(session.isEmpty());
        session = new CookieSession(new NewCookie("TEST", "foo=bar&&baz=qux"));
        assertTrue(session.isEmpty());
        session = new CookieSession(new NewCookie("TEST", "foo=bar&baz=qux"));
        assertFalse(session.isEmpty());
    }

    @Test
    public void testPutAndGet() {
        String[] secrets = {null, "testsecretkey"};
        for (String secret : secrets) {
            CookieSession session = new CookieSession(new NewCookie(
                    "TEST", "", "/", "",
                    NewCookie.DEFAULT_VERSION, "",
                    NewCookie.DEFAULT_MAX_AGE, false), secret);
            assertEquals(Optional.<String>absent(), session.get("nokey"));
            session.put("id", "1234");
            String ts = Long.toString(System.currentTimeMillis());
            session.put("timestamp", ts);
            assertEquals("1234", session.get("id").get());
            assertEquals(ts, session.get("timestamp").get());
        }
    }

    @Test
    public void testSignedCookie() {
        String secret = "testkey";
        CookieSession session = new CookieSession(new NewCookie(
                "TEST", "", "/", "localhost",
                NewCookie.DEFAULT_VERSION, "",
                NewCookie.DEFAULT_MAX_AGE, false), secret);
        session.put("id", "1234");
        session.put("flash", "Hello!");
        NewCookie cookie = session.toCookie();

        session = new CookieSession(cookie, secret);
        assertEquals("1234", session.get("id").get());
        assertEquals("Hello!", session.get("flash").get());

        String encoded = "id=1234&flash=Hello%21";
        String v = cookie.getValue();
        if (v.endsWith(encoded))
            assertEquals(
                    "d1dc24e66a830d6178634930502bf4207dab1726-id=1234&flash=Hello%21",
                    cookie.getValue());
    }
}
