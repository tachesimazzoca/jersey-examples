package app.models;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.google.common.base.Optional;

public class QuestionDao {
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
}
