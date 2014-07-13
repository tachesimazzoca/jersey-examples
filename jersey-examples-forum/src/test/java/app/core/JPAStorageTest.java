package app.core;

import static org.junit.Assert.*;

import org.junit.Test;

import javax.persistence.Persistence;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.google.common.base.Optional;

public class JPAStorageTest {
    private static EntityManagerFactory ef() {
        return Persistence.createEntityManagerFactory("default");
    }

    @Test
    public void testCRUD() {
        EntityManagerFactory ef = ef();
        EntityManager em = ef.createEntityManager();

        em.getTransaction().begin();
        em.createNativeQuery("TRUNCATE TABLE signup_storage").executeUpdate();
        em.getTransaction().commit();

        JPAStorage storage = new JPAStorage(ef, "signup_storage");

        Optional<String> vOpt;
        vOpt = storage.read("deadbeef");
        assertFalse(vOpt.isPresent());

        String key1 = storage.create("foo");
        vOpt = storage.read(key1);
        assertTrue(vOpt.isPresent());
        assertEquals("foo", vOpt.get());

        storage.write(key1, "bar");
        vOpt = storage.read(key1);
        assertTrue(vOpt.isPresent());
        assertEquals("bar", vOpt.get());

        String key2 = storage.create("baz");
        vOpt = storage.read(key2);
        assertTrue(vOpt.isPresent());
        assertEquals("baz", vOpt.get());

        vOpt = storage.read(key1);
        assertTrue(vOpt.isPresent());
        assertEquals("bar", vOpt.get());
        
        storage.delete(key1);
        vOpt = storage.read(key1);
        assertFalse(vOpt.isPresent());

        storage.delete(key2);
        vOpt = storage.read(key2);
        assertFalse(vOpt.isPresent());

        em.close();
    }
}
