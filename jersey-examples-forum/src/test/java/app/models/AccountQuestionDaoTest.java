package app.models;

import static org.junit.Assert.*;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;

import app.core.JPA;

import java.util.Map;

public class AccountQuestionDaoTest {
    private static EntityManagerFactory ef() {
        return JPA.ef();
    }

    @Test
    public void testGetPoint() {
        EntityManagerFactory ef = ef();

        AccountQuestionDao dao = new AccountQuestionDao(ef);

        Fixtures fixtures = new Fixtures(ef);
        fixtures.createAccounts(100);
        fixtures.createQuestions(10);
        fixtures.createAccountQuestions(100);

        Map<Long, Account> accountMap = fixtures.getRecordMap(Long.class, Account.class);
        Map<Long, Question> questionMap = fixtures.getRecordMap(Long.class, Question.class);
        for (Map.Entry<Long, Account> x : accountMap.entrySet()) {
            Long accountId = x.getKey();
            Map<Long, Integer> pointsMap = fixtures.getPointsMap("account_id = " + accountId);
            for (Map.Entry<Long, Question> y : questionMap.entrySet()) {
                Long questionId = y.getKey();
                assertEquals((int) pointsMap.get(questionId), dao.getPoint(accountId, questionId));
            }
        }
    }

    @Test
    public void testSumPositivePoints() {
        EntityManagerFactory ef = ef();

        AccountQuestionDao dao = new AccountQuestionDao(ef);

        Fixtures fixtures = new Fixtures(ef);
        fixtures.createAccounts(100);
        fixtures.createQuestions(10);
        fixtures.createAccountQuestions(100);

        Map<Long, Question> questionMap = fixtures.getRecordMap(Long.class, Question.class);
        Map<Long, Integer> positivePointsMap = fixtures.getPointsMap("point > 0");
        for (Map.Entry<Long, Question> entry : questionMap.entrySet()) {
            Long questionId = entry.getKey();
            assertEquals((int) positivePointsMap.get(questionId),
                    dao.sumPositivePoints(questionId));
        }
    }

    @Test
    public void testLog() {
        EntityManagerFactory ef = ef();

        AccountQuestionDao dao = new AccountQuestionDao(ef);

        Fixtures fixtures = new Fixtures(ef);
        fixtures.createAccountQuestions(0);

        dao.log(1L, 1L, 1);
        assertEquals(1, dao.getPoint(1L, 1L));
        assertEquals(0, dao.getPoint(1L, 2L));
        dao.log(1L, 2L, 1);
        assertEquals(1, dao.getPoint(1L, 2L));
        assertEquals(1, dao.getPoint(1L, 2L));
        dao.log(2L, 1L, 1);
        assertEquals(1, dao.getPoint(1L, 2L));
        assertEquals(1, dao.getPoint(1L, 2L));
        assertEquals(1, dao.getPoint(2L, 1L));
        assertEquals(0, dao.getPoint(2L, 2L));
    }
}
