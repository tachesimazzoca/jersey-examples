package app.db;

import app.core.storage.Storage;
import app.core.util.ObjectSerializer;
import com.google.common.base.Optional;
import org.apache.commons.io.IOUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.sql.Clob;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JPAStorage implements Storage<Map<String, Object>> {
    private final EntityManagerFactory ef;
    private final String INSERT_QUERY;
    private final String UPDATE_QUERY;
    private final String DELETE_QUERY;
    private final String SELECT_QUERY;
    private final String SELECT_FOR_UPDATE_QUERY;
    private final String prefix;

    public JPAStorage(EntityManagerFactory ef, String table) {
        this(ef, table, "");
    }

    public JPAStorage(EntityManagerFactory ef, String table, String prefix) {
        this.ef = ef;
        this.prefix = prefix;

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
    public String create(Map<String, Object> value) {
        String key = prefix + UUID.randomUUID().toString();
        write(key, value);
        return key;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Map<String, Object>> read(String key) {
        EntityManager em = ef.createEntityManager();
        List<Clob> rows = em.createNativeQuery(SELECT_QUERY)
                .setParameter(1, key)
                .getResultList();
        em.close();

        Map<String, Object> v = null;
        if (!rows.isEmpty()) {
            try {
                String encoded = IOUtils.toString(rows.get(0).getCharacterStream());
                v = (Map<String, Object>) ObjectSerializer.BASE64.deserialize(encoded, Map.class);
            } catch (Exception e) {
                // fail gracefully if the storage value is corrupted or type
                // mismatch.
                v = null;
            }
        }
        if (v != null)
            return Optional.of(v);
        else
            return Optional.absent();
    }

    @Override
    public void write(final String key, final Map<String, Object> value) {
        final String v = ObjectSerializer.BASE64.serialize(value);
        JPA.withTransaction(ef, new JPA.TransactionBlock<Void>() {
            public Void apply(EntityManager em) {
                List<?> rows = em.createNativeQuery(SELECT_FOR_UPDATE_QUERY)
                        .setParameter(1, key)
                        .getResultList();
                if (rows.isEmpty()) {
                    em.createNativeQuery(INSERT_QUERY)
                            .setParameter(1, key)
                            .setParameter(2, v)
                            .executeUpdate();
                } else {
                    em.createNativeQuery(UPDATE_QUERY)
                            .setParameter(1, v)
                            .setParameter(2, key)
                            .executeUpdate();
                }
                return null;
            }
        });
    }

    @Override
    public void delete(final String key) {
        JPA.withTransaction(ef, new JPA.TransactionBlock<Void>() {
            public Void apply(EntityManager em) {
                em.createNativeQuery(DELETE_QUERY)
                        .setParameter(1, key)
                        .executeUpdate();
                return null;
            }
        });
    }
}
