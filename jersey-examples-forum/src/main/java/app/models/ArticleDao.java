package app.models;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.google.common.base.Optional;

public class ArticleDao {
    private final EntityManagerFactory ef;

    public ArticleDao(EntityManagerFactory ef) {
        this.ef = ef;
    }

    public Optional<Article> find(long id) {
        EntityManager em = ef.createEntityManager();
        Article article = em.find(Article.class, id);
        em.close();
        return Optional.fromNullable(article);
    }

    public Article save(Article article) {
        EntityManager em = ef.createEntityManager();
        em.getTransaction().begin();
        if (article.getId() == null)
            em.persist(article);
        else
            em.merge(article);
        em.getTransaction().commit();
        em.close();
        return article;
    }
}
