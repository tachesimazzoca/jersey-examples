package app.models;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Map;

import app.core.JPA;
import app.core.Pagination;

public class QuestionsResultTest {
    private static final EntityManagerFactory ef = JPA.ef("test");

    @AfterClass
    public static void tearDown() {
        ef.close();
    }

    @Test
    public void testResultSetMappings() {
        Fixtures fixtures = new Fixtures(ef);
        fixtures.createAccounts(100);
        fixtures.createQuestions(10);
        fixtures.createAnswers(50);
        fixtures.createAccountQuestions(100);

        EntityManager em = ef.createEntityManager();

        Map<Long, Account> accountMap =
                fixtures.getRecordMap(Long.class, Account.class);
        Map<Long, Question> questionMap =
                fixtures.getRecordMap(Long.class, Question.class);
        Map<Long, Integer> numAnswersMap = fixtures.getNumAnswersMap();
        Map<Long, Integer> positivePointsMap = fixtures.getPointsMap("point > 0");
        Map<Long, Integer> negativePointsMap = fixtures.getPointsMap("point < 0");

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
                + " WHERE account_questions.question_id = questions.id"
                + " AND account_questions.point > 0)"
                + " AS positive_points,"
                + " (SELECT SUM(account_questions.point) FROM account_questions"
                + " WHERE account_questions.question_id = questions.id"
                + " AND account_questions.point < 0)"
                + " AS negative_points"
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
            assertEquals(positivePointsMap.get(result.getId()), result.getPositivePoints());
            assertEquals(negativePointsMap.get(result.getId()), result.getNegativePoints());
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
            assertEquals(positivePointsMap.get(result.getId()), result.getPositivePoints());
            assertEquals(negativePointsMap.get(result.getId()), result.getNegativePoints());
        }

        em.close();
    }
}
