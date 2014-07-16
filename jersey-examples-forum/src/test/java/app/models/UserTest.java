package app.models;

import static org.junit.Assert.*;
import org.junit.Test;

import org.apache.commons.codec.digest.DigestUtils;

public class UserTest {
    @Test
    public void testUpdatePassword() {
        User user = new User();
        user.updatePassword("foo", "bar");
        assertEquals("bar", user.getPasswordSalt());
        assertEquals(DigestUtils.sha1Hex("barfoo"), user.getPasswordHash());
    }
}
