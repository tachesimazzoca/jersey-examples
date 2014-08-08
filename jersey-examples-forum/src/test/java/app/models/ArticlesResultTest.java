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

public class ArticlesResultTest {
    private static EntityManagerFactory ef() {
        return JPA.ef();
    }

    @Test
    public void testResultSetMappings() {
        EntityManager em = ef().createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("TRUNCATE TABLE accounts").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE articles").executeUpdate();
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
        Map<Long, Article> questionMap = new HashMap<Long, Article>();
        for (int i = 0; i < maxAccounts * 2; i++) {
            em.getTransaction().begin();
            Article question = new Article();
            question.setAuthorId(accountIds[i % maxAccounts]);
            int n = i + 1;
            question.setSubject("subject" + n);
            question.setBody("body" + n);
            question.setPostedAt(new java.util.Date());
            em.persist(question);
            em.getTransaction().commit();
            questionMap.put(question.getId(), question);
        }
        for (Map.Entry<Long, Article> question : questionMap.entrySet()) {
            for (Map.Entry<Long, Account> account : accountMap.entrySet()) {
                em.getTransaction().begin();
                Article answer = new Article();
                answer.setParentId(question.getKey());
                answer.setAuthorId(account.getKey());
                String label = formatAnswerLabel(answer.getParentId(), answer.getAuthorId());
                answer.setSubject("subject " + label);
                answer.setBody("answer " + label);
                answer.setPostedAt(new java.util.Date());
                em.persist(answer);
                em.getTransaction().commit();
            }
        }

        String selectQuery = "SELECT"
                + " articles.id AS articles_id,"
                + " articles.subject AS articles_subject,"
                + " articles.posted_at AS articles_posted_at,"
                + " accounts.id AS accounts_id,"
                + " accounts.nickname AS accounts_nickname"
                + " FROM articles"
                + " LEFT JOIN accounts ON accounts.id = articles.author_id"
                + " WHERE articles.parent_id = ?1";

        String countQuery = "SELECT COUNT(*)"
                + " FROM articles"
                + " WHERE articles.parent_id = ?1";

        @SuppressWarnings("unchecked")
        List<ArticlesResult> results = em.createNativeQuery(
                selectQuery, "ArticlesResult").setParameter(1, 0).getResultList();
        assertEquals(questionMap.size(), results.size());
        for (ArticlesResult result : results) {
            Article question = questionMap.get(result.getId());
            Account author = accountMap.get(result.getAuthorId());
            assertEquals(question.getSubject(), result.getSubject());
            assertEquals(question.getPostedAt(), result.getPostedAt());
            assertEquals(author.getNickname(), result.getNickname());
        }

        Pagination<ArticlesResult> pagination =
                JPA.paginate(em, 0, 10,
                        em.createNativeQuery(countQuery).setParameter(1, 0),
                        em.createNativeQuery(selectQuery, "ArticlesResult").setParameter(1, 0),
                        ArticlesResult.class);
        assertEquals(questionMap.size(), pagination.getResults().size());
        for (ArticlesResult result : pagination.getResults()) {
            Article question = questionMap.get(result.getId());
            Account author = accountMap.get(result.getAuthorId());
            assertEquals(question.getSubject(), result.getSubject());
            assertEquals(question.getPostedAt(), result.getPostedAt());
            assertEquals(author.getNickname(), result.getNickname());
        }

        for (Map.Entry<Long, Article> entry : questionMap.entrySet()) {
            Long questionId = entry.getKey();
            Pagination<ArticlesResult> answers =
                    JPA.paginate(em, 0, 10,
                            em.createNativeQuery(countQuery).setParameter(1, questionId),
                            em.createNativeQuery(selectQuery, "ArticlesResult")
                                    .setParameter(1, questionId),
                            ArticlesResult.class);
            assertEquals(accountMap.size(), answers.getResults().size());
            for (ArticlesResult answer : answers.getResults()) {
                String label = formatAnswerLabel(questionId, answer.getAuthorId());
                assertEquals("subject " + label, answer.getSubject());
            }
        }
    }

    private static String formatAnswerLabel(Long parentId, Long authorId) {
        return String.format(
                "answer for %d by %d", parentId, authorId);
    }
}
