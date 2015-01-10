package app.core.session;

import org.junit.Test;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;

import java.util.HashMap;

import static org.junit.Assert.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CookieSessionFactoryTest {
    private void assertDefaultCookieSession(NewCookie cookie, String cookieName) {
        assertEquals(cookieName, cookie.getName());
        assertEquals("/", cookie.getPath());
        assertEquals("", cookie.getDomain());
        assertEquals("", cookie.getComment());
        assertEquals(NewCookie.DEFAULT_MAX_AGE, cookie.getMaxAge());
        assertEquals(NewCookie.DEFAULT_VERSION, cookie.getVersion());
        assertFalse(cookie.isSecure());
    }

    @Test
    public void testConstructorWithoutSecret() {
        CookieSessionFactory factory = new CookieSessionFactory("APP_SESSION");
        CookieSession session = factory.provide();
        assertDefaultCookieSession(session.toCookie(), "APP_SESSION");
    }

    @Test
    public void testConstructorWithSecret() {
        CookieSessionFactory factory = new CookieSessionFactory("APP_SESSION", "aSecret");
        CookieSession session = factory.provide();
        assertDefaultCookieSession(session.toCookie(), "APP_SESSION");
    }

    @Test
    public void testConstructorWithArguments() {
        CookieSessionFactory factory = new CookieSessionFactory(
                "APP_SESSION", "/foo", "www.example.net",
                1, "HttpOnly", 123, true, "aSecret");
        CookieSession session = factory.provide();
        NewCookie cookie = session.toCookie();
        assertEquals("APP_SESSION", cookie.getName());
        assertEquals("/foo", cookie.getPath());
        assertEquals("www.example.net", cookie.getDomain());
        assertEquals(1, cookie.getVersion());
        assertEquals("HttpOnly", cookie.getComment());
        assertEquals(123, cookie.getMaxAge());
        assertTrue(cookie.isSecure());
    }

    @Test
    public void testProvideWithEmptyCookies() {
        CookieSessionFactory factory = new CookieSessionFactory("APP_SESSION", "aSecret");
        HttpHeaders headers = mock(HttpHeaders.class);
        when(headers.getCookies()).thenReturn(new HashMap<String, Cookie>());
        factory.setHttpHeaders(headers);
        CookieSession session = factory.provide();
        assertDefaultCookieSession(session.toCookie(), "APP_SESSION");
    }

    @Test
    public void testProvideWithNoCookie() {
        CookieSessionFactory factory = new CookieSessionFactory("APP_SESSION");
        HttpHeaders headers = mock(HttpHeaders.class);
        HashMap<String, Cookie> cookies = new HashMap<String, Cookie>();
        cookies.put("DUMMY_COOKIE1", new Cookie("DUMMY_COOKIE1", "id=1"));
        cookies.put("DUMMY_COOKIE2", new Cookie("DUMMY_COOKIE2", "id=2"));
        when(headers.getCookies()).thenReturn(cookies);
        factory.setHttpHeaders(headers);
        CookieSession session = factory.provide();
        assertDefaultCookieSession(session.toCookie(), "APP_SESSION");
    }

    @Test
    public void testProvideWithCookie() {
        CookieSessionFactory factory = new CookieSessionFactory("APP_SESSION");
        HttpHeaders headers = mock(HttpHeaders.class);
        HashMap<String, Cookie> cookies = new HashMap<String, Cookie>();
        cookies.put("DUMMY_COOKIE1", new Cookie("DUMMY_COOKIE1", "id=1"));
        cookies.put("APP_SESSION", new Cookie("APP_SESSION", "id=1234"));
        cookies.put("DUMMY_COOKIE2", new Cookie("DUMMY_COOKIE2", "id=2"));
        when(headers.getCookies()).thenReturn(cookies);
        factory.setHttpHeaders(headers);
        CookieSession session = factory.provide();
        NewCookie cookie = session.toCookie();
        assertEquals("APP_SESSION", cookie.getName());
        assertEquals("id=1234", cookie.getValue());
    }

    @Test
    public void testProvideWithSecretCookie() {
        CookieSessionFactory factory = new CookieSessionFactory("APP_SESSION", "aSecret");
        HttpHeaders headers = mock(HttpHeaders.class);
        HashMap<String, Cookie> cookies = new HashMap<String, Cookie>();
        cookies.put("DUMMY_COOKIE1", new Cookie("DUMMY_COOKIE1", "id=1"));
        cookies.put("APP_SESSION", new Cookie("APP_SESSION",
                "c00c98a72f09f5d025b2298fa377bd3203a9555d-id=1234"));
        cookies.put("DUMMY_COOKIE2", new Cookie("DUMMY_COOKIE2", "id=2"));
        when(headers.getCookies()).thenReturn(cookies);
        factory.setHttpHeaders(headers);
        CookieSession session = factory.provide();
        NewCookie cookie = session.toCookie();
        assertEquals("APP_SESSION", cookie.getName());
        assertEquals("c00c98a72f09f5d025b2298fa377bd3203a9555d-id=1234", cookie.getValue());
    }
}
