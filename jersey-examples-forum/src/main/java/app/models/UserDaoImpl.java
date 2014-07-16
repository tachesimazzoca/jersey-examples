package app.models;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.google.common.base.Optional;

import java.util.List;

public class UserDaoImpl implements UserDao {
    private final EntityManagerFactory ef;

    public UserDaoImpl(EntityManagerFactory ef) {
        this.ef = ef;
    }

    public Optional<User> find(long id) {
        EntityManager em = ef.createEntityManager();
        User user = em.find(User.class, id);
        em.close();
        return Optional.<User> of(user);
    }

    public Optional<User> findByEmail(String email) {
        EntityManager em = ef.createEntityManager();
        List<User> rows = em.createQuery(
                "SELECT a FROM User a WHERE a.email = :email", User.class)
                .setParameter("email", email)
                .setMaxResults(1)
                .getResultList();
        em.close();
        if (rows.isEmpty())
            return Optional.<User> absent();
        else
            return Optional.<User> of(rows.get(0));
    }

    public User save(User user) {
        if (user.getId() == null && user.getPasswordHash().isEmpty())
            throw new IllegalArgumentException();
        EntityManager em = ef.createEntityManager();
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
        em.refresh(user);
        em.close();
        return user;
    }
}
