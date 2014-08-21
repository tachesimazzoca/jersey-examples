package app.models;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;

import java.util.Map;

import app.core.JPA;

public class AccountAnswerDaoTest {
    private static final EntityManagerFactory ef = JPA.ef("test");

    @AfterClass
    public static void tearDown() {
        ef.close();
    }

    @Test
    public void testGetPoint() {
        AccountAnswerDao dao = new AccountAnswerDao(ef);

        Fixtures fixtures = new Fixtures(ef);
        fixtures.createAccounts(100);
        fixtures.createQuestions(10);
        fixtures.createAnswers(50);
        fixtures.createAccountAnswers(100);

        Map<Long, Account> accountMap = fixtures.getRecordMap(Long.class, Account.class);
        Map<Long, Answer> answerMap = fixtures.getRecordMap(Long.class, Answer.class);
        for (Map.Entry<Long, Account> x : accountMap.entrySet()) {
            Long accountId = x.getKey();
            Map<Long, Integer> pointsMap = fixtures.getAnswerPointsMap("account_id = " + accountId);
            for (Map.Entry<Long, Answer> y : answerMap.entrySet()) {
                Long answerId = y.getKey();
                assertEquals((int) pointsMap.get(answerId), dao.getPoint(accountId, answerId));
            }
        }
    }

    @Test
    public void testSumPoints() {
        AccountAnswerDao dao = new AccountAnswerDao(ef);

        Fixtures fixtures = new Fixtures(ef);
        fixtures.createAccounts(100);
        fixtures.createQuestions(10);
        fixtures.createAnswers(50);
        fixtures.createAccountAnswers(100);

        Map<Long, Answer> answerMap = fixtures.getRecordMap(Long.class, Answer.class);
        Map<Long, Integer> positivePointsMap = fixtures.getAnswerPointsMap("point > 0");
        Map<Long, Integer> negativePointsMap = fixtures.getAnswerPointsMap("point < 0");
        for (Map.Entry<Long, Answer> entry : answerMap.entrySet()) {
            Long answerId = entry.getKey();
            int ppts = positivePointsMap.get(answerId);
            int npts = negativePointsMap.get(answerId);
            assertEquals(ppts, dao.sumPositivePoints(answerId));
            assertEquals(npts, dao.sumNegativePoints(answerId));
        }
    }

    @Test
    public void testLog() {
        AccountAnswerDao dao = new AccountAnswerDao(ef);

        Fixtures fixtures = new Fixtures(ef);
        fixtures.createAccountAnswers(0);

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
