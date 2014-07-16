package app.models;

import static org.junit.Assert.*;
import org.junit.Test;

import org.apache.commons.codec.digest.DigestUtils;

public class UserTest {
    @Test(expected = IllegalArgumentException.class)
    public void testRefreshPasswordWithNullSalt() {
        User user = new User();
        user.refreshPassword("foo", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRefreshPasswordWithEmptySalt() {
        User user = new User();
        user.refreshPassword("foo", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRefreshPasswordWithInvalidLengthSalt() {
        User user = new User();
        user.refreshPassword("foo", "12345");
    }

    @Test
    public void testRefreshPassword() {
        User user = new User();
        user.refreshPassword("foo", "salt");
        assertEquals("salt", user.getPasswordSalt());
        assertEquals(DigestUtils.sha1Hex("saltfoo"), user.getPasswordHash());
    }

    @Test
    public void testIsEqualPassword() {
        final String password = "pass";
        User user = new User();
        user.refreshPassword(password);
        assertTrue(user.isEqualPassword(password));
    }
}
