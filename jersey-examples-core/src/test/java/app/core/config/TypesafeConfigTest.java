package app.core.config;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.base.Optional;

public class TypesafeConfigTest {
    @Test
    public void testMaybe() {
        Config config = TypesafeConfig.load("test/conf/test");
        assertEquals(Optional.absent(), config.maybe("unknown"));
        assertEquals("/", config.maybe("url.base").or("/foo"));
        assertEquals("TEST", config.maybe("session.cookieName").or("TEST"));
    }
}
