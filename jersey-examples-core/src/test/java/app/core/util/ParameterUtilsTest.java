package app.core.util;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class ParameterUtilsTest {
    @Test
    public void testParams() {
        Map<String, Object> m = ParameterUtils.params("A", 1, "B", 2, "C", 3);
        assertEquals(1, m.get("A"));
        assertEquals(2, m.get("B"));
        assertEquals(3, m.get("C"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParamsWithInvalidArgs() {
        Map<String, Object> m = ParameterUtils.params("A", 1, "B", 2, "C");
    }

    @Test
    public void testNullTo() {
        assertEquals("foo", ParameterUtils.nullTo("foo"));
        assertEquals(null, ParameterUtils.nullTo((String) null));
        assertEquals("foo", ParameterUtils.nullTo(null, null, "foo"));
        assertEquals("foo", ParameterUtils.nullTo(null, "foo", "bar"));
        assertEquals("foo", ParameterUtils.nullTo("foo", "bar"));
        assertEquals(null, ParameterUtils.nullTo(null, null, null));
    }
}
