package app.models;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;

import java.util.Map;

import app.core.JPA;
import app.core.Pagination;

public class QuestionDaoTest {
    private static final EntityManagerFactory ef = JPA.ef("test");

    @AfterClass
    public static void tearDown() {
        ef.close();
    }

    @Test
    public void testSelect() {
        Fixtures fixtures = new Fixtures(ef);
        fixtures.createAccounts(200);
        fixtures.createQuestions(50);
        fixtures.createAnswers(200);
        fixtures.createAccountQuestions(200);

        Map<Long, Account> accountMap =
                fixtures.getRecordMap(Long.class, Account.class);
        Map<Long, Question> questionMap =
                fixtures.getRecordMap(Long.class, Question.class);
        Map<Long, Integer> numAnswersMap = fixtures.getNumAnswersMap();
        Map<Long, Integer> positivePointsMap = fixtures.getQuestionPointsMap("point > 0");
        Map<Long, Integer> negativePointsMap = fixtures.getQuestionPointsMap("point < 0");

        QuestionDao questionDao = new QuestionDao(ef);

        assertQuestionsResult(questionDao.selectPublicQuestions(0, 50),
                questionMap, accountMap, numAnswersMap, positivePointsMap, negativePointsMap);

        for (Map.Entry<Long, Account> entry : accountMap.entrySet()) {
            assertQuestionsResult(questionDao.selectByAuthorId(entry.getKey(), 0, 50),
                    questionMap, accountMap, numAnswersMap, positivePointsMap, negativePointsMap);
        }
    }

    private void assertQuestionsResult(
            Pagination<QuestionsResult> pagination,
            Map<Long, Question> questionMap,
            Map<Long, Account> accountMap,
            Map<Long, Integer> numAnswersMap,
            Map<Long, Integer> positivePointsMap,
            Map<Long, Integer> negativePointsMap) {

        assertTrue(pagination.getLimit() >= pagination.getResults().size());
        for (QuestionsResult result : pagination.getResults()) {
            Question question = questionMap.get(result.getId());
            Account author = accountMap.get(result.getAuthorId());
            assertEquals(question.getSubject(), result.getSubject());
            assertEquals(question.getPostedAt(), result.getPostedAt());
            assertEquals(author.getNickname(), result.getNickname());
            assertEquals(numAnswersMap.get(result.getId()), result.getNumAnswers());
            int ppts = positivePointsMap.get(result.getId());
            int npts = negativePointsMap.get(result.getId());
            assertEquals(ppts + npts, (int) result.getSumPoints());
            assertEquals(ppts, (int) result.getPositivePoints());
            assertEquals(npts, (int) result.getNegativePoints());
        }
    }
}
