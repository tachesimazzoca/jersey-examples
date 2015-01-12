package app.models;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import app.db.JPA;
import app.db.JPADao;
import app.core.util.Pagination;

public class AnswerDao extends JPADao<Answer> {
    private static final String SELECT_ANSWER_RESULT = "SELECT"
            + " answers.id,"
            + " answers.question_id,"
            + " answers.body,"
            + " answers.posted_at,"
            + " answers.status,"
            + " accounts.id,"
            + " accounts.nickname,"
            + " IFNULL((SELECT SUM(account_answers.point) FROM account_answers"
            + " WHERE account_answers.answer_id = answers.id), 0)"
            + " AS sum_points,"
            + " IFNULL((SELECT SUM(account_answers.point) FROM account_answers"
            + " WHERE account_answers.answer_id = answers.id"
            + " AND account_answers.point > 0), 0)"
            + " AS positive_points,"
            + " IFNULL((SELECT SUM(account_answers.point) FROM account_answers"
            + " WHERE account_answers.answer_id = answers.id"
            + " AND account_answers.point < 0), 0)"
            + " AS negative_points"
            + " FROM answers"
            + " LEFT JOIN accounts ON accounts.id = answers.author_id";

    private static final String COUNT_ANSWER_RESULT =
            "SELECT COUNT(*) FROM answers";

    public AnswerDao(EntityManagerFactory ef) {
        super(ef, Answer.class);
    }

    public Answer save(Answer answer) {
        if (answer.getId() == null)
            return create(answer);
        else
            return update(answer);
    }

    public void updateStatus(final Long id, final Answer.Status status) {
        withTransaction(new JPA.TransactionBlock<Integer>() {
            public Integer apply(EntityManager em) {
                return em.createNativeQuery("UPDATE answers SET status = ?1 WHERE id = ?2")
                        .setParameter(1, status.getValue())
                        .setParameter(2, id).executeUpdate();
            }
        });
    }

    public Pagination<AnswersResult> selectByQuestionId(
            Long questionId, int offset, int limit) {
        EntityManager em = ef.createEntityManager();
        String where = " WHERE answers.status = 0 AND answers.question_id = ?1";
        String countQuery = COUNT_ANSWER_RESULT + where;
        String selectQuery = SELECT_ANSWER_RESULT + where
                + " ORDER BY sum_points DESC, answers.id ASC";
        Pagination<AnswersResult> pagination = JPA.paginate(em, offset, limit,
                em.createNativeQuery(countQuery).setParameter(1, questionId),
                em.createNativeQuery(selectQuery, "AnswersResult").setParameter(1, questionId),
                AnswersResult.class);
        em.close();
        return pagination;
    }

    public Pagination<AnswersResult> selectByAuthorId(
            Long authorId, int offset, int limit) {
        EntityManager em = ef.createEntityManager();
        String where = " WHERE answers.status IN (0, 2) AND answers.author_id = ?1";
        String countQuery = COUNT_ANSWER_RESULT + where;
        String selectQuery = SELECT_ANSWER_RESULT + where
                + " ORDER BY sum_points DESC, answers.id ASC";
        Pagination<AnswersResult> pagination = JPA.paginate(em, offset, limit,
                em.createNativeQuery(countQuery).setParameter(1, authorId),
                em.createNativeQuery(selectQuery, "AnswersResult").setParameter(1, authorId),
                AnswersResult.class);
        em.close();
        return pagination;
    }
}
