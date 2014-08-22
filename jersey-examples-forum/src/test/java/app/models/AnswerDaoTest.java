package app.models;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;

import java.util.Map;

import app.core.JPA;
import app.core.Pagination;

public class AnswerDaoTest {
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
        fixtures.createAccountAnswers(200);

        Map<Long, Account> accountMap =
                fixtures.getRecordMap(Long.class, Account.class);
        Map<Long, Question> questionMap =
                fixtures.getRecordMap(Long.class, Question.class);
        Map<Long, Answer> answerMap =
                fixtures.getRecordMap(Long.class, Answer.class);
        Map<Long, Integer> positivePointsMap = fixtures.getAnswerPointsMap("point > 0");
        Map<Long, Integer> negativePointsMap = fixtures.getAnswerPointsMap("point < 0");

        AnswerDao answerDao = new AnswerDao(ef);

        for (Map.Entry<Long, Question> entry : questionMap.entrySet()) {
            Long questionId = entry.getKey();
            Pagination<AnswersResult> r1 = answerDao.selectByQuestionId(questionId, 0, 5);
            assertAnswersResult(r1, answerMap, accountMap, positivePointsMap, negativePointsMap);
            if (r1.getCount() == 0)
                continue;
            Pagination<AnswersResult> r2 = answerDao.selectByQuestionId(questionId, 0, 1);
            assertEquals(r1.getResults().get(0).getId(), r2.getResults().get(0).getId());
        }
        for (Map.Entry<Long, Account> entry : accountMap.entrySet()) {
            Long authorId = entry.getKey();
            assertAnswersResult(answerDao.selectByAuthorId(authorId, 0, 10),
                    answerMap, accountMap, positivePointsMap, negativePointsMap);
        }
    }

    private void assertAnswersResult(
            Pagination<AnswersResult> pagination,
            Map<Long, Answer> answerMap,
            Map<Long, Account> accountMap,
            Map<Long, Integer> positivePointsMap,
            Map<Long, Integer> negativePointsMap) {

        assertTrue(pagination.getLimit() >= pagination.getResults().size());
        for (AnswersResult result : pagination.getResults()) {
            Answer answer = answerMap.get(result.getId());
            Account author = accountMap.get(result.getAuthorId());
            assertEquals(answer.getQuestionId(), result.getQuestionId());
            assertEquals(answer.getBody(), result.getBody());
            assertEquals(answer.getPostedAt(), result.getPostedAt());
            assertEquals(author.getNickname(), result.getNickname());
            int ppts = positivePointsMap.get(result.getId());
            int npts = negativePointsMap.get(result.getId());
            assertEquals(ppts + npts, (int) result.getSumPoints());
            assertEquals(ppts, (int) result.getPositivePoints());
            assertEquals(npts, (int) result.getNegativePoints());
        }
    }
}
