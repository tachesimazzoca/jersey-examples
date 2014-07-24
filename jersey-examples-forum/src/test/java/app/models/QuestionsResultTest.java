package app.models;

import static org.junit.Assert.*;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.core.JPA;

public class QuestionsResultTest {
    private static EntityManagerFactory ef() {
        return JPA.ef();
    }

    @Test
    public void testResultSetMappings() {
        EntityManager em = ef().createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("TRUNCATE TABLE accounts").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE questions").executeUpdate();
        em.getTransaction().commit();
        final int maxAccounts = 5;
        Map<Long, Account> accountMap = new HashMap<Long, Account>();
        long[] accountIds = new long[maxAccounts];
        for (int i = 0; i < maxAccounts; i++) {
            em.getTransaction().begin();
            Account account = new Account();
            account.setEmail("user" + (i + 1) + "@example.net");
            account.refreshPassword("xxxx");
            em.persist(account);
            em.getTransaction().commit();
            accountMap.put(account.getId(), account);
            accountIds[i] = account.getId();
        }
        Map<Long, Question> questionMap = new HashMap<Long, Question>();
        for (int i = 0; i < maxAccounts * 2; i++) {
            em.getTransaction().begin();
            Question question = new Question();
            question.setAuthorId(accountIds[i % maxAccounts]);
            int n = i + 1;
            question.setSubject("subject" + n);
            question.setBody("body" + n);
            question.setPostedAt(new java.util.Date());
            em.persist(question);
            em.getTransaction().commit();
            questionMap.put(question.getId(), question);
        }

        @SuppressWarnings("unchecked")
        List<QuestionsResult> results = em.createNativeQuery(
                "SELECT"
                        + " questions.id AS questions_id,"
                        + " questions.subject AS questions_subject,"
                        + " questions.posted_at AS questions_posted_at,"
                        + " accounts.id AS accounts_id,"
                        + " accounts.email AS accounts_email"
                        + " FROM questions"
                        + " LEFT JOIN accounts ON accounts.id = questions.author_id",
                "QuestionsResult").getResultList();
        for (QuestionsResult result : results) {
            Question question = questionMap.get(result.getId());
            Account author = accountMap.get(result.getAuthorId());
            assertEquals(question.getSubject(), result.getSubject());
            assertEquals(question.getPostedAt(), result.getPostedAt());
            assertEquals(author.getEmail(), result.getEmail());
        }
    }
}
