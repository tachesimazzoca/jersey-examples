package app.models;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigInteger;

import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang.reflect.MethodUtils;

public class Fixtures {
    private EntityManagerFactory ef;

    public Fixtures(EntityManagerFactory ef) {
        this.ef = ef;
    }

    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> getRecordMap(Class<K> keyType, Class<V> valueType) {
        String table = valueType.getSimpleName();
        Map<K, V> m = new HashMap<K, V>();
        EntityManager em = ef.createEntityManager();
        List<V> rows = em.createQuery("SELECT a FROM " + table + " a", valueType)
                .getResultList();
        em.close();
        for (V row : rows) {
            K id = null;
            try {
                id = (K) MethodUtils.invokeMethod(row, "getId", null);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException(e);
            }
            m.put(id, row);
        }
        return m;
    }

    public void createAccounts(int max) {
        EntityManager em = ef.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("TRUNCATE TABLE accounts").executeUpdate();
        em.getTransaction().commit();
        for (int i = 0; i < max; i++) {
            em.getTransaction().begin();
            Account account = new Account();
            account.setEmail("user" + (i + 1) + "@example.net");
            account.refreshPassword("xxxx");
            account.setNickname("user" + (i + 1));
            em.persist(account);
            em.getTransaction().commit();
        }
    }

    public void createQuestions(int max) {
        EntityManager em = ef.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("TRUNCATE TABLE questions").executeUpdate();
        em.getTransaction().commit();

        Map<Long, Account> accountMap = getRecordMap(Long.class, Account.class);

        int maxAccounts = accountMap.size();
        Long[] accountIds = accountMap.keySet().toArray(new Long[maxAccounts]);

        for (int i = 0; i < max; i++) {
            Long authorId = accountIds[(int) (Math.random() * maxAccounts)];
            em.getTransaction().begin();
            Question question = new Question();
            question.setAuthorId(authorId);
            question.setSubject("subject" + i);
            question.setBody("question" + i);
            question.setPostedAt(new java.util.Date());
            em.persist(question);
            em.getTransaction().commit();
        }
        em.close();
    }

    public void createAnswers(int max) {
        EntityManager em = ef.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("TRUNCATE TABLE answers").executeUpdate();
        em.getTransaction().commit();

        Map<Long, Account> accountMap = getRecordMap(Long.class, Account.class);
        Map<Long, Question> questionMap = getRecordMap(Long.class, Question.class);

        int maxAccounts = accountMap.size();
        Long[] accountIds = accountMap.keySet().toArray(new Long[maxAccounts]);
        int maxQuestions = questionMap.size();
        Long[] questionIds = questionMap.keySet().toArray(new Long[maxQuestions]);

        for (int i = 0; i < max; i++) {
            Long authorId = accountIds[(int) (Math.random() * maxAccounts)];
            Long questionId = questionIds[(int) (Math.random() * maxQuestions)];
            em.getTransaction().begin();
            Answer answer = new Answer();
            answer.setQuestionId(questionId);
            answer.setAuthorId(authorId);
            answer.setBody("answer" + i);
            answer.setPostedAt(new java.util.Date());
            em.persist(answer);
            em.getTransaction().commit();
        }
        em.close();
    }

    public void createAccountQuestions(int max) {
        EntityManager em = ef.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("TRUNCATE TABLE account_questions").executeUpdate();
        em.getTransaction().commit();

        Map<Long, Account> accountMap = getRecordMap(Long.class, Account.class);
        Map<Long, Question> questionMap = getRecordMap(Long.class, Question.class);

        int maxAccounts = accountMap.size();
        Long[] accountIds = accountMap.keySet().toArray(new Long[maxAccounts]);
        int maxQuestions = questionMap.size();
        Long[] questionIds = questionMap.keySet().toArray(new Long[maxQuestions]);

        for (int i = 0; i < max; i++) {
            Long accountId = accountIds[(int) (Math.random() * maxAccounts)];
            Long questionId = questionIds[(int) (Math.random() * maxQuestions)];
            int point = ((int) (Math.random() * 2)) * 2 - 1;
            em.getTransaction().begin();
            em.createNativeQuery("INSERT INTO account_questions VALUES (?1, ?2, ?3, NOW())")
                    .setParameter(1, accountId)
                    .setParameter(2, questionId)
                    .setParameter(3, point)
                    .executeUpdate();
            em.getTransaction().commit();
        }
        em.close();
    }

    public Map<Long, Integer> getNumAnswersMap() {
        EntityManager em = ef.createEntityManager();

        Map<Long, Integer> m = new HashMap<Long, Integer>();
        Map<Long, Question> questionMap = getRecordMap(Long.class, Question.class);
        for (Long questionId : questionMap.keySet()) {
            m.put(questionId, 0);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> results = (List<Object[]>) em.createNativeQuery(
                "SELECT question_id, COUNT(*) FROM answers"
                        + " WHERE status = 0 GROUP BY question_id")
                .getResultList();
        em.close();
        for (Object[] result : results) {
            m.put((Long) ((BigInteger) result[0]).longValue(),
                    ((BigInteger) result[1]).intValue());
        }
        return m;
    }

    public Map<Long, Integer> getPointsMap(String where) {
        EntityManager em = ef.createEntityManager();
        Map<Long, Integer> m = new HashMap<Long, Integer>();
        Map<Long, Question> questionMap = getRecordMap(Long.class, Question.class);
        for (Long questionId : questionMap.keySet()) {
            m.put(questionId, 0);
        }

        String sql = "SELECT question_id, SUM(point) FROM account_questions";
        if (where != null && !where.isEmpty()) {
            sql += " WHERE " + where;
        }
        sql += " GROUP BY question_id";

        @SuppressWarnings("unchecked")
        List<Object[]> results = (List<Object[]>) em.createNativeQuery(sql).getResultList();
        em.close();
        for (Object[] result : results) {
            m.put((Long) ((BigInteger) result[0]).longValue(),
                    ((BigInteger) result[1]).intValue());
        }
        return m;
    }
}
