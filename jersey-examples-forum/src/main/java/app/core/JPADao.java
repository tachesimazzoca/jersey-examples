package app.core;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.google.common.base.Optional;

public class JPADao<V> {
    protected final Class<V> valueType;
    protected final EntityManagerFactory ef;

    public JPADao(EntityManagerFactory ef, Class<V> valueType) {
        this.valueType = valueType;
        this.ef = ef;
    }

    public <K> Optional<V> find(K id) {
        EntityManager em = ef.createEntityManager();
        V entity = em.find(valueType, id);
        em.close();
        return Optional.fromNullable(entity);
    }

    public V create(final V entity) {
        return JPA.withTransaction(ef, new JPA.TransactionBlock<V>() {
            public V apply(EntityManager em) {
                em.persist(entity);
                return entity;
            }
        });
    }

    public V update(final V entity) {
        return JPA.withTransaction(ef, new JPA.TransactionBlock<V>() {
            public V apply(EntityManager em) {
                em.merge(entity);
                return entity;
            }
        });
    }

    public <E> E withTransaction(JPA.TransactionBlock<E> block) {
        return JPA.withTransaction(ef, block);
    }
}
