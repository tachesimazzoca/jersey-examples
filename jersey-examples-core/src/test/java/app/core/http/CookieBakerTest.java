package app.core.http;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import javax.ws.rs.core.NewCookie;
import java.util.List;

import static org.junit.Assert.*;

public class CookieBakerTest {
    @Test
    public void testEmptyCookie() {
        Optional<String> secret = Optional.absent();
        NewCookie emptyCookie = new NewCookie(
                "TEST", "", "/test", "localhost",
                NewCookie.DEFAULT_VERSION, "HTTPOnly",
                -86400, true);
        CookieBaker baker = new CookieBaker(secret, emptyCookie);
        assertEquals(Optional.<String> absent(), baker.get("nokey"));
        assertEquals(emptyCookie, baker.toCookie());
    }

    @Test
    public void testInvalidCookie() {
        Optional<String> secret = Optional.absent();
        CookieBaker baker;
        baker = new CookieBaker(secret, new NewCookie("TEST", "dead"));
        assertTrue(baker.isEmpty());
        baker = new CookieBaker(secret, new NewCookie("TEST", "foo="));
        assertTrue(baker.isEmpty());
        baker = new CookieBaker(secret, new NewCookie("TEST", "=bar"));
        assertTrue(baker.isEmpty());
        baker = new CookieBaker(secret, new NewCookie("TEST", "&foo=bar"));
        assertTrue(baker.isEmpty());
        baker = new CookieBaker(secret, new NewCookie("TEST", "foo=bar&"));
        assertTrue(baker.isEmpty());
        baker = new CookieBaker(secret, new NewCookie("TEST", "foo=bar&&baz=qux"));
        assertTrue(baker.isEmpty());
        baker = new CookieBaker(secret, new NewCookie("TEST", "foo=bar&baz=qux"));
        assertFalse(baker.isEmpty());
    }

    @Test
    public void testPutAndGet() {
        List<Optional<String>> secrets = ImmutableList.of(
                Optional.<String> absent(), Optional.of("testsecretkey"));
        for (Optional<String> secret : secrets) {
            CookieBaker baker = new CookieBaker(secret, new NewCookie(
                    "TEST", "", "/", "",
                    NewCookie.DEFAULT_VERSION, "",
                    NewCookie.DEFAULT_MAX_AGE, false));
            assertEquals(Optional.<String> absent(), baker.get("nokey"));
            baker.put("id", "1234");
            String ts = Long.toString(System.currentTimeMillis());
            baker.put("timestamp", ts);
            assertEquals("1234", baker.get("id").get());
            assertEquals(ts, baker.get("timestamp").get());
        }
    }

    @Test
    public void testSignedCookie() {
        String key = "testkey";
        Optional<String> secret = Optional.of(key);
        CookieBaker baker = new CookieBaker(secret, new NewCookie(
                "TEST", "", "/", "localhost",
                NewCookie.DEFAULT_VERSION, "",
                NewCookie.DEFAULT_MAX_AGE, false));
        baker.put("id", "1234");
        baker.put("flash", "Hello!");
        NewCookie cookie = baker.toCookie();

        String encoded = "id=1234&flash=Hello%21";
        assertEquals(
                "d1dc24e66a830d6178634930502bf4207dab1726-id=1234&flash=Hello%21",
                cookie.getValue());

        baker = new CookieBaker(secret, cookie);
        assertEquals("1234", baker.get("id").get());
        assertEquals("Hello!", baker.get("flash").get());
    }
}
