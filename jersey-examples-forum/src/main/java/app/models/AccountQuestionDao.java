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
        }
        em.close();
    }

    public void log(long accountId, long questionId) {
        log(accountId, questionId, 0);
    }
}
