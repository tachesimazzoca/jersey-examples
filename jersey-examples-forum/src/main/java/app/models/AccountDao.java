package app.models;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.google.common.base.Optional;

import java.util.List;

import app.core.JPADao;

public class AccountDao extends JPADao<Account> {
    public AccountDao(EntityManagerFactory ef) {
        super(ef, Account.class);
    }

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

    public Account save(Account account) {
        if (account.getId() == null && account.getPasswordHash().isEmpty())
            throw new IllegalArgumentException();
        if (account.getId() == null)
            return create(account);
        else
            return update(account);
    }
}
