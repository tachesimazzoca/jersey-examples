package app.models;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.google.common.base.Optional;

import app.core.JPA;
import app.core.Pagination;

public class ArticleDao {
    private final EntityManagerFactory ef;

    public ArticleDao(EntityManagerFactory ef) {
        this.ef = ef;
    }

    public Optional<Article> find(long id) {
        EntityManager em = ef.createEntityManager();
        Article question = em.find(Article.class, id);
        em.close();
        return Optional.fromNullable(question);
    }

    public Article save(Article question) {
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

    public Pagination<ArticlesResult> selectQuestions(int offset, int limit) {
        EntityManager em = ef.createEntityManager();

        String selectQuery = "SELECT"
                + " articles.id AS articles_id,"
                + " articles.subject AS articles_subject,"
                + " articles.posted_at AS articles_posted_at,"
                + " accounts.id AS accounts_id,"
                + " accounts.nickname AS accounts_nickname"
                + " FROM articles"
                + " LEFT JOIN accounts ON accounts.id = articles.author_id"
                + " WHERE articles.parent_id = ?1"
                + " ORDER BY articles.posted_at DESC";

        String countQuery = "SELECT COUNT(*)"
                + " FROM articles"
                + " WHERE articles.parent_id = ?1";

        Pagination<ArticlesResult> pagination = JPA.paginate(em, offset, limit,
                em.createNativeQuery(countQuery).setParameter(1, 0L),
                em.createNativeQuery(selectQuery, "ArticlesResult").setParameter(1, 0L),
                ArticlesResult.class);
        em.close();

        return pagination;
    }
}
