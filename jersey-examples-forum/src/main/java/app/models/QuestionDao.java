package app.models;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.google.common.base.Optional;

import app.core.JPA;
import app.core.Pagination;

public class QuestionDao {
    private static final String SELECT_QUESTIONS_RESULT = "SELECT"
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

    private static final String COUNT_QUESTIONS_RESULT =
            "SELECT COUNT(*) FROM questions";

    private final EntityManagerFactory ef;

    public QuestionDao(EntityManagerFactory ef) {
        this.ef = ef;
    }

    public Optional<Question> find(long id) {
        EntityManager em = ef.createEntityManager();
        Question question = em.find(Question.class, id);
        em.close();
        return Optional.fromNullable(question);
    }

    public Question save(Question question) {
        EntityManager em = ef.createEntityManager();
        em.getTransaction().begin();
        if (question.getId() == null)
            em.persist(question);
        else
            em.merge(question);
        em.getTransaction().commit();
        em.close();
        return question;
    }

    public void updateStatus(Long id, Question.Status status) {
        EntityManager em = ef.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("UPDATE questions SET status = ?1 WHERE id = ?2")
                .setParameter(1, status.getValue())
                .setParameter(2, id).executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    public Pagination<QuestionsResult> selectPublicQuestions(int offset, int limit) {
        EntityManager em = ef.createEntityManager();
        String where = " WHERE questions.status = 0";
        String countQuery = COUNT_QUESTIONS_RESULT + where;
        String selectQuery = SELECT_QUESTIONS_RESULT + where
                + " ORDER BY questions.posted_at DESC";
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
                + " ORDER BY questions.posted_at DESC";
        Pagination<QuestionsResult> pagination = JPA.paginate(em, offset, limit,
                em.createNativeQuery(countQuery).setParameter(1, authorId),
                em.createNativeQuery(selectQuery, "QuestionsResult").setParameter(1, authorId),
                QuestionsResult.class);
        em.close();
        return pagination;
    }
}
