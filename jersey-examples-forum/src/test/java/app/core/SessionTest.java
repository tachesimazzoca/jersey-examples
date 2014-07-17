package app.core;

import static org.junit.Assert.*;
import org.junit.Test;

import javax.ws.rs.core.Cookie;

import com.google.common.base.Optional;

public class SessionTest {
    @Test
    public void testPutAndGet() {
        Session sess = new Session(new Cookie("test", "", "/", ""));
        assertEquals(Optional.<String> absent(), sess.get("nokey"));
        sess.put("id", 1234);
        long t = System.currentTimeMillis();
        sess.put("timestamp", t);
        assertEquals(1234, sess.get("id").get());
        assertEquals(t, sess.get("timestamp").get());
    }
}
