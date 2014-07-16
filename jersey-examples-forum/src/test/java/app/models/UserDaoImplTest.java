package app.models;

import static org.junit.Assert.*;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;

import app.core.JPA;

public class UserDaoImplTest {
    private static EntityManagerFactory ef() {
        return JPA.ef();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveEmptyUser() {
        EntityManagerFactory ef = ef();
        UserDao dao = new UserDaoImpl(ef);
        dao.save(new User());
    }

    @Test
    public void testValidUser() {
        EntityManagerFactory ef = ef();
        UserDao dao = new UserDaoImpl(ef);
        User user1 = new User();
        user1.setEmail("user1@example.net");
        user1.refreshPassword("1111", "xxxx");
        User savedUser1 = dao.save(user1);

        User user2 = new User();
        user2.setEmail("user2@example.net");
        user2.refreshPassword("2222", "xxxx");
        User savedUser2 = dao.save(user2);

        user1 = dao.find(savedUser1.getId()).get();
        assertEquals(savedUser1, user1);
        user1 = dao.findByEmail("user1@example.net").get();
        assertEquals(savedUser1, user1);

        user2 = dao.find(savedUser2.getId()).get();
        assertEquals(savedUser2, user2);
        user2 = dao.findByEmail("user2@example.net").get();
        assertEquals(savedUser2, user2);
    }

    @Test(expected = javax.persistence.PersistenceException.class)
    public void testEmailConflict() {
        EntityManagerFactory ef = ef();
        UserDao dao = new UserDaoImpl(ef);
        User user1 = new User();
        user1.setEmail("user1@example.net");
        user1.refreshPassword("1111", "xxxx");
        dao.save(user1);

        User user2 = new User();
        user2.setEmail("user1@example.net");
        user2.refreshPassword("2222", "xxxx");
        dao.save(user2);
    }
}
