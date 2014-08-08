package app.models;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.google.common.base.Optional;

import app.core.JPA;
import app.core.Pagination;

public class AnswerDao {
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
        String countQuery = "SELECT COUNT(*) FROM answers WHERE question_id = ?1";
        String selectQuery = "SELECT"
                + " answers.id,"
                + " answers.question_id,"
                + " answers.body,"
                + " answers.posted_at,"
                + " accounts.id,"
                + " accounts.nickname"
                + " FROM answers"
                + " LEFT JOIN accounts ON accounts.id = answers.author_id"
                + " WHERE answers.question_id = ?1"
                + " ORDER BY answers.posted_at ASC";
        Pagination<AnswersResult> pagination = JPA.paginate(em, offset, limit,
                em.createNativeQuery(countQuery).setParameter(1, questionId),
                em.createNativeQuery(selectQuery, "AnswersResult").setParameter(1, questionId),
                AnswersResult.class);
        em.close();
        return pagination;
    }
}
