package app.models;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.math.BigInteger;

public class AccountQuestionDao {
    private final EntityManagerFactory ef;

    public AccountQuestionDao(EntityManagerFactory ef) {
        this.ef = ef;
    }

    public void log(long accountId, long questionId, int point) {
        EntityManager em = ef.createEntityManager();
        BigInteger c = (BigInteger) em.createNativeQuery(
                "SELECT COUNT(*) FROM account_questions"
                        + " WHERE account_id = ?1 AND question_id = ?2")
                .setParameter(1, accountId)
                .setParameter(2, questionId)
                .getSingleResult();
        if (c.intValue() == 0) {
            em.getTransaction().begin();
            em.createNativeQuery(
                    "INSERT INTO account_questions"
                            + " (account_id, question_id, point, modified_at)"
                            + " VALUES (?1, ?2, ?3, ?4)")
                    .setParameter(1, accountId)
                    .setParameter(2, questionId)
                    .setParameter(3, point)
                    .setParameter(4, new java.util.Date())
                    .executeUpdate();
            em.getTransaction().commit();
        } else {
            em.getTransaction().begin();
            em.createNativeQuery(
                    "UPDATE account_questions SET point = ?1"
                            + " WHERE account_id = ?2 AND question_id = ?3")
                    .setParameter(1, point)
                    .setParameter(2, accountId)
                    .setParameter(3, questionId)
                    .executeUpdate();
            em.getTransaction().commit();
        }
        em.close();
    }

    private int sumPoints(Long questionId, String where) {
        EntityManager em = ef.createEntityManager();
        BigInteger sum = (BigInteger) em.createNativeQuery(
                "SELECT SUM(point) FROM account_questions"
                        + " WHERE question_id = ?1 AND " + where)
                .setParameter(1, questionId)
                .getSingleResult();
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
        if (sum == null)
            return 0;
        else
            return sum.intValue();
    }
}
