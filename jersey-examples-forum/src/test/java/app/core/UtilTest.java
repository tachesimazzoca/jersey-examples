package app.core;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

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
}
