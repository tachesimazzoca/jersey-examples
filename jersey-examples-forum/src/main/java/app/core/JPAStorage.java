package app.core;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.google.common.base.Optional;

import java.util.UUID;
import java.util.List;

import java.sql.Clob;

import org.apache.commons.io.IOUtils;

public class JPAStorage implements Storage {
    private final EntityManagerFactory ef;
    private final String INSERT_QUERY;
    private final String UPDATE_QUERY;
    private final String DELETE_QUERY;
    private final String SELECT_QUERY;
    private final String SELECT_FOR_UPDATE_QUERY;

    public JPAStorage(EntityManagerFactory ef, String table) {
        this.ef = ef;

        INSERT_QUERY = "INSERT INTO " + table
                + " (storage_key, storage_value, storage_timestamp)"
                + " VALUES (?1, ?2, NOW())";

        UPDATE_QUERY = "UPDATE " + table
                + " SET storage_value = ?1, storage_timestamp = NOW()"
                + " WHERE storage_key = ?2";

        DELETE_QUERY = "DELETE FROM " + table + " WHERE storage_key = ?1";

        SELECT_QUERY = "SELECT storage_value FROM " + table
                + " WHERE storage_key = ?1 LIMIT 1";

        SELECT_FOR_UPDATE_QUERY = "SELECT storage_value FROM " + table
                + " WHERE storage_key = ?1 LIMIT 1 FOR UPDATE";
    }

    @Override
    public String create(String value) {
        String key = UUID.randomUUID().toString();
        write(key, value);
        return key;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<String> read(String key) {
        EntityManager em = ef.createEntityManager();
        List<Clob> rows = em.createNativeQuery(SELECT_QUERY)
                .setParameter(1, key)
                .getResultList();
        em.close();

        Optional<String> v;
        if (rows.isEmpty()) {
            v = Optional.<String> absent();
        } else {
            try {
                v = Optional.<String> of(IOUtils.toString(rows.get(0).getCharacterStream()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return v;
    }

    @Override
    public void write(String key, String value) {
        EntityManager em = ef.createEntityManager();
        em.getTransaction().begin();
        List<?> rows = em.createNativeQuery(SELECT_FOR_UPDATE_QUERY)
                .setParameter(1, key)
                .getResultList();
        if (rows.isEmpty()) {
            em.createNativeQuery(INSERT_QUERY)
                    .setParameter(1, key)
                    .setParameter(2, value)
                    .executeUpdate();
        } else {
            em.createNativeQuery(UPDATE_QUERY)
                    .setParameter(1, value)
                    .setParameter(2, key)
                    .executeUpdate();
        }
        em.getTransaction().commit();
        em.close();
    }

    @Override
    public void delete(String key) {
        EntityManager em = ef.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery(DELETE_QUERY)
                .setParameter(1, key)
                .executeUpdate();
        em.getTransaction().commit();
        em.close();
    }
}
