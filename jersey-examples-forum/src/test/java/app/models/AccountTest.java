package app.models;

import static org.junit.Assert.*;
import org.junit.Test;

import org.apache.commons.codec.digest.DigestUtils;

public class AccountTest {
    @Test(expected = IllegalArgumentException.class)
    public void testRefreshPasswordWithNullSalt() {
        Account account = new Account();
        account.refreshPassword("foo", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRefreshPasswordWithEmptySalt() {
        Account account = new Account();
        account.refreshPassword("foo", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRefreshPasswordWithInvalidLengthSalt() {
        Account account = new Account();
        account.refreshPassword("foo", "12345");
    }

    @Test
    public void testRefreshPassword() {
        Account account = new Account();
        account.refreshPassword("foo", "salt");
        assertEquals("salt", account.getPasswordSalt());
        assertEquals(DigestUtils.sha1Hex("saltfoo"), account.getPasswordHash());
    }

    @Test
    public void testIsEqualPassword() {
        final String password = "pass";
        Account account = new Account();
        account.refreshPassword(password);
        assertTrue(account.isEqualPassword(password));
    }
}
