package app.models;

import static org.junit.Assert.*;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.core.JPA;
import app.core.Pagination;

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
            account.setNickname("user" + (i + 1));
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

        String selectQuery = "SELECT"
                + " questions.id,"
                + " questions.subject,"
                + " questions.body,"
                + " questions.posted_at,"
                + " questions.status,"
                + " accounts.id,"
                + " accounts.nickname"
                + " FROM questions"
                + " LEFT JOIN accounts ON accounts.id = questions.author_id";

        String countQuery = "SELECT COUNT(*) FROM questions";

        @SuppressWarnings("unchecked")
        List<QuestionsResult> results = em.createNativeQuery(
                selectQuery, "QuestionsResult").getResultList();
        assertEquals(questionMap.size(), results.size());
        for (QuestionsResult result : results) {
            Question question = questionMap.get(result.getId());
            Account author = accountMap.get(result.getAuthorId());
            assertEquals(question.getSubject(), result.getSubject());
            assertEquals(question.getPostedAt(), result.getPostedAt());
            assertEquals(author.getNickname(), result.getNickname());
        }

        Pagination<QuestionsResult> pagination =
                JPA.paginate(em, 0, 10,
                        em.createNativeQuery(countQuery),
                        em.createNativeQuery(selectQuery, "QuestionsResult"),
                        QuestionsResult.class);
        assertEquals(questionMap.size(), pagination.getResults().size());
        for (QuestionsResult result : pagination.getResults()) {
            Question question = questionMap.get(result.getId());
            Account author = accountMap.get(result.getAuthorId());
            assertEquals(question.getSubject(), result.getSubject());
            assertEquals(question.getPostedAt(), result.getPostedAt());
            assertEquals(author.getNickname(), result.getNickname());
        }
    }
}
