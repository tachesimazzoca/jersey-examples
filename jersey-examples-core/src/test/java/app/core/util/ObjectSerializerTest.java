package app.core.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

public class ObjectSerializerTest {
    @Test
    public void testBase64() throws IOException, ClassNotFoundException {
        ObjectSerializer.Serializer serializer = ObjectSerializer.BASE64;
        String s = "deadbeef";
        String ser = serializer.serialize(s);
        assertEquals(s, serializer.deserialize(ser, String.class));

        Map<String, Object> m = ImmutableMap.of("foo", (Object) 1234, "bar", "baz");
        ser = serializer.serialize(m);
        Map<?, ?> n = serializer.deserialize(ser, Map.class);
        assertEquals(m, n);
        assertEquals(1234, n.get("foo"));
        assertEquals("baz", n.get("bar"));
    }
}
