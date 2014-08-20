package app.models;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;

import app.core.JPA;

public class AccountDaoTest {
    private static final EntityManagerFactory ef = JPA.ef("test");

    @AfterClass
    public static void tearDown() {
        ef.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveEmptyAccount() {
        AccountDao dao = new AccountDao(ef);
        dao.save(new Account());
    }

    @Test
    public void testValidAccount() {
        AccountDao dao = new AccountDao(ef);
        Account account1 = new Account();
        account1.setEmail("account1@example.net");
        account1.refreshPassword("1111", "xxxx");
        Account savedAccount1 = dao.save(account1);

        Account account2 = new Account();
        account2.setEmail("account2@example.net");
        account2.refreshPassword("2222", "xxxx");
        Account savedAccount2 = dao.save(account2);

        account1 = dao.find(savedAccount1.getId()).get();
        assertEquals(savedAccount1, account1);
        account1 = dao.findByEmail("account1@example.net").get();
        assertEquals(savedAccount1, account1);

        account2 = dao.find(savedAccount2.getId()).get();
        assertEquals(savedAccount2, account2);
        account2 = dao.findByEmail("account2@example.net").get();
        assertEquals(savedAccount2, account2);
    }

    @Test(expected = javax.persistence.PersistenceException.class)
    public void testEmailConflict() {
        AccountDao dao = new AccountDao(ef);
        Account account1 = new Account();
        account1.setEmail("account1@example.net");
        account1.refreshPassword("1111", "xxxx");
        dao.save(account1);

        Account account2 = new Account();
        account2.setEmail("account1@example.net");
        account2.refreshPassword("2222", "xxxx");
        dao.save(account2);
    }
}
