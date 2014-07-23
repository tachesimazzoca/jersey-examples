package app.models;

import static org.junit.Assert.*;

import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.math.BigInteger;

import app.core.JPA;

public class ArticleTest {
    private static EntityManagerFactory ef() {
        return JPA.ef();
    }

    @Test
    public void testPersistWithEmptyAssociation() {
        EntityManagerFactory ef = ef();
        EntityManager em = ef.createEntityManager();
        Article article = new Article();
        article.setSubject("test subject");
        article.setBody("test body");
        long t = System.currentTimeMillis();
        article.setPostedAt(new java.util.Date(t));
        em.getTransaction().begin();
        em.persist(article);
        em.getTransaction().commit();
        Long id = article.getId();
        Article article2 = em.find(Article.class, id);
        assertEquals((Long) 0L, article2.getAuthorId());
        assertEquals((Long) 0L, article2.getParentId());
        BigInteger parentId = (BigInteger) em.createNativeQuery(
                "SELECT parent_id FROM articles WHERE id = ?1")
                .setParameter(1, id)
                .getSingleResult();
        assertEquals(BigInteger.valueOf(0L), parentId);
        BigInteger authorId = (BigInteger) em.createNativeQuery(
                "SELECT author_id FROM articles WHERE id = ?1")
                .setParameter(1, id)
                .getSingleResult();
        assertEquals(BigInteger.valueOf(0L), authorId);
        em.close();
    }
}
