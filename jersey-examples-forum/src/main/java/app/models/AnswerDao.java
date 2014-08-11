package app.models;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.google.common.base.Optional;

import app.core.JPA;
import app.core.Pagination;

public class AnswerDao {
    private static final String SELECT_ANSWER_RESULT = "SELECT"
            + " answers.id,"
            + " answers.question_id,"
            + " answers.body,"
            + " answers.posted_at,"
            + " answers.status,"
            + " accounts.id,"
            + " accounts.nickname"
            + " FROM answers"
            + " LEFT JOIN accounts ON accounts.id = answers.author_id";

    private static final String COUNT_ANSWER_RESULT =
            "SELECT COUNT(*) FROM answers";

    private final EntityManagerFactory ef;

    public AnswerDao(EntityManagerFactory ef) {
        this.ef = ef;
    }

    public Optional<Answer> find(long id) {
        EntityManager em = ef.createEntityManager();
        Answer answer = em.find(Answer.class, id);
        em.close();
        return Optional.fromNullable(answer);
    }

    public Answer save(Answer answer) {
        EntityManager em = ef.createEntityManager();
        em.getTransaction().begin();
        if (answer.getId() == null)
            em.persist(answer);
        else
            em.merge(answer);
        em.getTransaction().commit();
        em.close();
        return answer;
    }

    public Pagination<AnswersResult> selectByQuestionId(
            Long questionId, int offset, int limit) {
        EntityManager em = ef.createEntityManager();
        String where = " WHERE answers.question_id = ?1";
        String countQuery = COUNT_ANSWER_RESULT + where;
        String selectQuery = SELECT_ANSWER_RESULT + where
                + " ORDER BY answers.posted_at ASC";
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
        String where = " WHERE answers.author_id = ?1";
        String countQuery = COUNT_ANSWER_RESULT + where;
        String selectQuery = SELECT_ANSWER_RESULT + where
                + " ORDER BY answers.posted_at DESC";
        Pagination<AnswersResult> pagination = JPA.paginate(em, offset, limit,
                em.createNativeQuery(countQuery).setParameter(1, authorId),
                em.createNativeQuery(selectQuery, "AnswersResult").setParameter(1, authorId),
                AnswersResult.class);
        em.close();
        return pagination;
    }
}
