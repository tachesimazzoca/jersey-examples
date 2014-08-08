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
            + " accounts.id,"
            + " accounts.nickname"
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

    public Pagination<QuestionsResult> select(int offset, int limit) {
        EntityManager em = ef.createEntityManager();
        Pagination<QuestionsResult> pagination = JPA.paginate(em, offset, limit,
                em.createNativeQuery(COUNT_QUESTIONS_RESULT),
                em.createNativeQuery(
                        SELECT_QUESTIONS_RESULT + " ORDER BY questions.posted_at DESC",
                        "QuestionsResult"),
                QuestionsResult.class);
        em.close();
        return pagination;
    }
}
