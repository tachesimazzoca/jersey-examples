package app.core;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.util.Map;

import com.google.common.base.Optional;

import static app.core.Util.params;

public class JPAStorageTest {
    private static final EntityManagerFactory ef = JPA.ef("test");

    @AfterClass
    public static void tearDown() {
        ef.close();
    }

    @Test
    public void testCRUD() {
        EntityManager em = ef.createEntityManager();

        em.getTransaction().begin();
        em.createNativeQuery("TRUNCATE TABLE session_storage").executeUpdate();
        em.getTransaction().commit();

        JPAStorage storage = new JPAStorage(ef, "session_storage");

        Optional<Map<String, Object>> vOpt;
        Map<String, Object> m;

        vOpt = storage.read("deadbeef");
        assertFalse(vOpt.isPresent());

        String key1 = storage.create(params("a", "1", "b", "2"));
        vOpt = storage.read(key1);
        assertTrue(vOpt.isPresent());
        m = vOpt.get();
        assertEquals("1", m.get("a"));
        assertEquals("2", m.get("b"));

        storage.write(key1, params("foo", 123));
        vOpt = storage.read(key1);
        assertTrue(vOpt.isPresent());
        m = vOpt.get();
        assertEquals(123, m.get("foo"));

        // String key2 = storage.create("baz");
        // vOpt = storage.read(key2, String.class);
        // assertTrue(vOpt.isPresent());
        // assertEquals("baz", vOpt.get());

        // vOpt = storage.read(key1, String.class);
        // assertTrue(vOpt.isPresent());
        // assertEquals("bar", vOpt.get());

        // storage.delete(key1);
        // vOpt = storage.read(key1, String.class);
        // assertFalse(vOpt.isPresent());

        // storage.delete(key2);
        // vOpt = storage.read(key2, String.class);
        // assertFalse(vOpt.isPresent());

        em.close();
    }

    @Test
    public void testPrefix() {
        EntityManager em = ef.createEntityManager();

        em.getTransaction().begin();
        em.createNativeQuery("TRUNCATE TABLE session_storage").executeUpdate();
        em.getTransaction().commit();
        em.close();

        JPAStorage storage = new JPAStorage(ef, "session_storage", "signup-");
        String key1 = storage.create(params());
        assertTrue(key1.startsWith("signup-"));
    }
}
