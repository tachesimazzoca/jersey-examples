package app.models;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.math.BigInteger;

import app.db.JPA;

public class QuestionTest {
    private static final EntityManagerFactory ef = JPA.ef("test");

    @AfterClass
    public static void tearDown() {
        ef.close();
    }

    @Test
    public void testPersistWithEmptyAssociation() {
        EntityManager em = ef.createEntityManager();
        Question question = new Question();
        question.setSubject("test subject");
        question.setBody("test body");
        long t = System.currentTimeMillis();
        question.setPostedAt(new java.util.Date(t));
        em.getTransaction().begin();
        em.persist(question);
        em.getTransaction().commit();
        Long id = question.getId();
        question = em.find(Question.class, id);
        assertEquals((Long) 0L, question.getAuthorId());
        BigInteger authorId = (BigInteger) em.createNativeQuery(
                "SELECT author_id FROM questions WHERE id = ?1")
                .setParameter(1, id)
                .getSingleResult();
        assertEquals(BigInteger.valueOf(0L), authorId);
        em.close();
    }
}
