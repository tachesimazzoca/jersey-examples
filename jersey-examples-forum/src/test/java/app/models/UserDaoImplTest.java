package app.models;

import static org.junit.Assert.*;
import org.junit.Test;

import javax.persistence.Persistence;
import javax.persistence.EntityManagerFactory;

public class UserDaoImplTest {
    private static EntityManagerFactory ef() {
        return Persistence.createEntityManagerFactory("default");
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
        User user = new User();
        user.setEmail("foo@example.net");
        user.updatePassword("1234", "abcd");
        user = dao.save(user);
        Long id = user.getId();
        user = dao.find(id).get();
        assertEquals("foo@example.net", user.getEmail());
        assertEquals("abcd", user.getPasswordSalt());
        user = dao.findByEmail("foo@example.net").get();
        assertEquals(id, user.getId());
        assertEquals("abcd", user.getPasswordSalt());
    }
}
