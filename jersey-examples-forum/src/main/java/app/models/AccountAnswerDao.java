package app.models;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.math.BigInteger;

import app.db.JPA;

public class AccountAnswerDao {
    private final EntityManagerFactory ef;

    public AccountAnswerDao(EntityManagerFactory ef) {
        this.ef = ef;
    }

    public void log(final long accountId, final long answerId, final int point) {
        JPA.withTransaction(ef, new JPA.TransactionBlock<Void>() {
            public Void apply(EntityManager em) {
                em.createNativeQuery(
                        "DELETE FROM account_answers WHERE account_id = ?1 AND answer_id = ?2")
                        .setParameter(1, accountId)
                        .setParameter(2, answerId)
                        .executeUpdate();
                em.createNativeQuery(
                        "INSERT INTO account_answers"
                                + " (account_id, answer_id, point, modified_at)"
                                + " VALUES (?1, ?2, ?3, ?4)")
                        .setParameter(1, accountId)
                        .setParameter(2, answerId)
                        .setParameter(3, point)
                        .setParameter(4, new java.util.Date())
                        .executeUpdate();
                return null;
            }
        });
    }

    private int sumPoints(Long answerId, String where) {
        EntityManager em = ef.createEntityManager();
        BigInteger sum = (BigInteger) em.createNativeQuery(
                "SELECT SUM(point) FROM account_answers"
                        + " WHERE answer_id = ?1 AND " + where)
                .setParameter(1, answerId)
                .getSingleResult();
        em.close();
        if (sum == null)
            return 0;
        else
            return sum.intValue();
    }

    public int sumPositivePoints(Long answerId) {
        return sumPoints(answerId, "point > 0");
    }

    public int sumNegativePoints(Long answerId) {
        return sumPoints(answerId, "point < 0");
    }

    public int getPoint(Long accountId, Long answerId) {
        EntityManager em = ef.createEntityManager();
        BigInteger sum = (BigInteger) em.createNativeQuery(
                "SELECT SUM(point) FROM account_answers"
                        + " WHERE account_id = ?1 AND answer_id = ?2")
                .setParameter(1, accountId)
                .setParameter(2, answerId)
                .getSingleResult();
        em.close();
        if (sum == null)
            return 0;
        else
            return sum.intValue();
    }
}
