package app.models;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.math.BigInteger;

import app.db.JPA;

public class AccountQuestionDao {
    private final EntityManagerFactory ef;

    public AccountQuestionDao(EntityManagerFactory ef) {
        this.ef = ef;
    }

    public void log(final long accountId, final long questionId, final int point) {
        JPA.withTransaction(ef, new JPA.TransactionBlock<Void>() {
            public Void apply(EntityManager em) {
                em.createNativeQuery(
                        "DELETE account_questions WHERE account_id = ?1 AND question_id = ?2")
                        .setParameter(1, accountId)
                        .setParameter(2, questionId)
                        .executeUpdate();
                em.createNativeQuery(
                        "INSERT INTO account_questions"
                                + " (account_id, question_id, point, modified_at)"
                                + " VALUES (?1, ?2, ?3, ?4)")
                        .setParameter(1, accountId)
                        .setParameter(2, questionId)
                        .setParameter(3, point)
                        .setParameter(4, new java.util.Date())
                        .executeUpdate();
                return null;
            }
        });
    }

    private int sumPoints(Long questionId, String where) {
        EntityManager em = ef.createEntityManager();
        BigInteger sum = (BigInteger) em.createNativeQuery(
                "SELECT SUM(point) FROM account_questions"
                        + " WHERE question_id = ?1 AND " + where)
                .setParameter(1, questionId)
                .getSingleResult();
        em.close();
        if (sum == null)
            return 0;
        else
            return sum.intValue();
    }

    public int sumPositivePoints(Long questionId) {
        return sumPoints(questionId, "point > 0");
    }

    public int sumNegativePoints(Long questionId) {
        return sumPoints(questionId, "point < 0");
    }

    public int getPoint(Long accountId, Long questionId) {
        EntityManager em = ef.createEntityManager();
        BigInteger sum = (BigInteger) em.createNativeQuery(
                "SELECT SUM(point) FROM account_questions"
                        + " WHERE account_id = ?1 AND question_id = ?2")
                .setParameter(1, accountId)
                .setParameter(2, questionId)
                .getSingleResult();
        em.close();
        if (sum == null)
            return 0;
        else
            return sum.intValue();
    }
}
