package app.models;

import app.core.util.Pagination;
import app.db.JPA;
import app.db.JPADao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class QuestionDao extends JPADao<Question> {
    private static final String SELECT_QUESTIONS_RESULT = "SELECT"
            + " questions.id,"
            + " questions.subject,"
            + " questions.body,"
            + " questions.posted_at,"
            + " questions.status,"
            + " accounts.id,"
            + " accounts.nickname,"
            + " IFNULL((SELECT COUNT(*) FROM answers"
            + " WHERE answers.question_id = questions.id AND answers.status = 0), 0)"
            + " AS num_answers,"
            + " IFNULL((SELECT SUM(account_questions.point) FROM account_questions"
            + " WHERE account_questions.question_id = questions.id), 0)"
            + " AS sum_points,"
            + " IFNULL((SELECT SUM(account_questions.point) FROM account_questions"
            + " WHERE account_questions.question_id = questions.id"
            + " AND account_questions.point > 0), 0)"
            + " AS positive_points,"
            + " IFNULL((SELECT SUM(account_questions.point) FROM account_questions"
            + " WHERE account_questions.question_id = questions.id"
            + " AND account_questions.point < 0), 0)"
            + " AS negative_points"
            + " FROM questions"
            + " LEFT JOIN accounts ON accounts.id = questions.author_id";

    private static final String COUNT_QUESTIONS_RESULT =
            "SELECT COUNT(*) FROM questions";

    public QuestionDao(EntityManagerFactory ef) {
        super(ef, Question.class);
    }

    public Question save(Question question) {
        if (question.getId() == null)
            return create(question);
        else
            return update(question);
    }

    public void updateStatus(final Long id, final Question.Status status) {
        withTransaction(new JPA.TransactionBlock<Integer>() {
            public Integer apply(EntityManager em) {
                return em.createNativeQuery("UPDATE questions SET status = ?1 WHERE id = ?2")
                        .setParameter(1, status.getValue())
                        .setParameter(2, id).executeUpdate();
            }
        });
    }

    public Pagination<QuestionsResult> selectPublicQuestions(
            int offset, int limit, QuestionsResult.OrderBy orderBy) {
        EntityManager em = ef.createEntityManager();
        String where = " WHERE questions.status = 0";
        String countQuery = COUNT_QUESTIONS_RESULT + where;
        String selectQuery = SELECT_QUESTIONS_RESULT + where
                + " ORDER BY " + orderBy.getCaluse();
        Pagination<QuestionsResult> pagination = JPA.paginate(em, offset, limit,
                em.createNativeQuery(countQuery),
                em.createNativeQuery(selectQuery, "QuestionsResult"),
                QuestionsResult.class);
        em.close();
        return pagination;
    }

    public Pagination<QuestionsResult> selectByAuthorId(Long authorId, int offset, int limit) {
        EntityManager em = ef.createEntityManager();
        String where = " WHERE questions.status IN (0, 2) AND questions.author_id = ?1";
        String countQuery = COUNT_QUESTIONS_RESULT + where;
        String selectQuery = SELECT_QUESTIONS_RESULT + where
                + " ORDER BY " + QuestionsResult.OrderBy.defaultValue().getCaluse();
        Pagination<QuestionsResult> pagination = JPA.paginate(em, offset, limit,
                em.createNativeQuery(countQuery).setParameter(1, authorId),
                em.createNativeQuery(selectQuery, "QuestionsResult").setParameter(1, authorId),
                QuestionsResult.class);
        em.close();
        return pagination;
    }
}
