package app.core.session;

import app.core.storage.FakeStorage;
import org.junit.Test;

import javax.ws.rs.core.NewCookie;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class StorageSessionTest {
    @Test
    public void testConstructorWithEmptySessionId() {
        NewCookie emptyCookie = new NewCookie("APP_SESSION_ID", "");
        StorageSession session = new StorageSession(
                new FakeStorage<Map<String, Object>>(), emptyCookie);
        NewCookie cookie = session.toCookie();
        assertFalse(cookie.getValue().isEmpty());
    }

    @Test
    public void testConstructorWithValidSessionId() {
        String sessionId = "023ca0e1-eb33-4114-a67d-7db5f8c060e8";
        NewCookie emptyCookie = new NewCookie("APP_SESSION_ID", "");

        // Prepare some entries of the session ID because of avoiding ID re-generation.
        FakeStorage<Map<String, Object>> storage = new FakeStorage<Map<String, Object>>();
        storage.write(sessionId, new HashMap<String, Object>());

        StorageSession session = new StorageSession(storage, emptyCookie, sessionId);
        NewCookie cookie = session.toCookie();
        assertEquals(sessionId, cookie.getValue());
    }

    @Test
    public void testPutAndGet() {
        NewCookie emptyCookie = new NewCookie("APP_SESSION_ID", "");
        StorageSession session = new StorageSession(
                new FakeStorage<Map<String, Object>>(), emptyCookie);
        session.put("foo", "valueOfFoo");
        session.put("bar", "valueOfBar");
        session.put("baz", "valueOfBaz");
        assertEquals("valueOfFoo", session.get("foo").orNull());
        assertEquals("valueOfBar", session.get("bar").orNull());
        assertEquals("valueOfBaz", session.get("baz").orNull());
        session.remove("bar");
        assertNull(session.get("bar").orNull());
    }
}
