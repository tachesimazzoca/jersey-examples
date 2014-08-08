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
        Article question = new Article();
        question.setSubject("test subject");
        question.setBody("test body");
        long t = System.currentTimeMillis();
        question.setPostedAt(new java.util.Date(t));
        em.getTransaction().begin();
        em.persist(question);
        em.getTransaction().commit();
        Long id = question.getId();
        question = em.find(Article.class, id);
        assertEquals((Long) 0L, question.getAuthorId());
        BigInteger authorId = (BigInteger) em.createNativeQuery(
                "SELECT author_id FROM articles WHERE id = ?1")
                .setParameter(1, id)
                .getSingleResult();
        assertEquals(BigInteger.valueOf(0L), authorId);
        em.close();
    }
}
