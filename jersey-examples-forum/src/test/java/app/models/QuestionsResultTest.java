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

    private void fixture(
            EntityManager em,
            Map<Long, Account> accountMap,
            Map<Long, Question> questionMap,
            Map<Long, Integer> numAnswersMap,
            Map<Long, Integer> numPointsMap) {
        final int maxAccounts = 5;
        final int maxQuestions = 10;
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

        for (int i = 0; i < maxQuestions; i++) {
            em.getTransaction().begin();
            Question question = new Question();
            question.setAuthorId(accountIds[i % maxAccounts]);
            int n = i + 1;
            question.setSubject("subject" + n);
            question.setBody("question" + n);
            question.setPostedAt(new java.util.Date());
            em.persist(question);
            em.getTransaction().commit();
            questionMap.put(question.getId(), question);
        }

        for (Map.Entry<Long, Question> entry : questionMap.entrySet()) {
            Long questionId = entry.getKey();
            int max = (int) (Math.random() * maxAccounts);
            for (int i = 0; i < max; i++) {
                em.getTransaction().begin();
                Answer answer = new Answer();
                answer.setQuestionId(questionId);
                answer.setAuthorId(accountIds[i % maxAccounts]);
                int n = i + 1;
                answer.setBody("answer" + n);
                answer.setPostedAt(new java.util.Date());
                em.persist(answer);
                em.getTransaction().commit();
            }
            numAnswersMap.put(questionId, max);
        }

        for (Map.Entry<Long, Question> entry : questionMap.entrySet()) {
            Long questionId = entry.getKey();
            int numPoints = 0;
            for (int i = 0; i < maxAccounts; i++) {
                em.getTransaction().begin();
                int point = ((int) (Math.random() * 2)) * 2 - 1;
                em.createNativeQuery("INSERT INTO account_questions VALUES (?1, ?2, ?3, NOW())")
                        .setParameter(1, accountIds[i % maxAccounts])
                        .setParameter(2, questionId)
                        .setParameter(3, point)
                        .executeUpdate();
                em.getTransaction().commit();
                numPoints += point;
            }
            numPointsMap.put(questionId, numPoints);
        }
    }

    @Test
    public void testResultSetMappings() {
        EntityManager em = ef().createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("TRUNCATE TABLE accounts").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE questions").executeUpdate();
        em.getTransaction().commit();

        Map<Long, Account> accountMap = new HashMap<Long, Account>();
        Map<Long, Question> questionMap = new HashMap<Long, Question>();
        Map<Long, Integer> numAnswersMap = new HashMap<Long, Integer>();
        Map<Long, Integer> numPointsMap = new HashMap<Long, Integer>();

        fixture(em, accountMap, questionMap, numAnswersMap, numPointsMap);

        String selectQuery = "SELECT"
                + " questions.id,"
                + " questions.subject,"
                + " questions.body,"
                + " questions.posted_at,"
                + " questions.status,"
                + " accounts.id,"
                + " accounts.nickname,"
                + " (SELECT COUNT(*) FROM answers"
                + " WHERE answers.question_id = questions.id AND answers.status = 0)"
                + " AS num_answers,"
                + " (SELECT SUM(account_questions.point) FROM account_questions"
                + " WHERE account_questions.question_id = questions.id)"
                + " AS num_points"
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
            assertEquals(numAnswersMap.get(result.getId()), result.getNumAnswers());
            assertEquals(numPointsMap.get(result.getId()), result.getNumPoints());
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
            assertEquals(numAnswersMap.get(result.getId()), result.getNumAnswers());
            assertEquals(numPointsMap.get(result.getId()), result.getNumPoints());
        }
    }
}
