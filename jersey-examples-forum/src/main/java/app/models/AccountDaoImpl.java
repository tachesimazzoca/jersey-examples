package app.models;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.google.common.base.Optional;

import java.util.List;

public class AccountDaoImpl implements AccountDao {
    private final EntityManagerFactory ef;

    public AccountDaoImpl(EntityManagerFactory ef) {
        this.ef = ef;
    }

    @Override
    public Optional<Account> find(long id) {
        EntityManager em = ef.createEntityManager();
        Account account = em.find(Account.class, id);
        em.close();
        return Optional.fromNullable(account);
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        EntityManager em = ef.createEntityManager();
        List<Account> rows = em.createQuery(
                "SELECT a FROM Account a WHERE a.email = :email", Account.class)
                .setParameter("email", email)
                .setMaxResults(1)
                .getResultList();
        em.close();
        if (rows.isEmpty())
            return Optional.<Account> absent();
        else
            return Optional.<Account> of(rows.get(0));
    }

    @Override
    public Account save(Account account) {
        if (account.getId() == null && account.getPasswordHash().isEmpty())
            throw new IllegalArgumentException();
        EntityManager em = ef.createEntityManager();
        em.getTransaction().begin();
        if (account.getId() == null)
            em.persist(account);
        else
            em.merge(account);
        em.getTransaction().commit();
        em.close();
        return account;
    }
}
