package app.core.mail;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.junit.Test;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;

import static org.junit.Assert.*;

public class TextMailerFactoryTest {
    @Test
    public void testCreate() throws
            IOException,
            MessagingException,
            EmailException {
        TextMailerFactory factory = new TextMailerFactory();
        factory.setFake(true);
        factory.setHostName("smtp.example.net");
        factory.setSmtpPort(2525);
        factory.setCharset("UTF-8");
        factory.setSubject("Test Subject");
        InternetAddress from = new InternetAddress();
        from.setAddress("support@example.net");
        from.setPersonal("Test Support");
        factory.setFrom(from);

        CommonsMailer mailer = (CommonsMailer) factory.create("user@example.net", "Test Message");
        Email email = mailer.getEmail();
        assertEquals("smtp.example.net", email.getHostName());
        assertEquals("2525", email.getSmtpPort());
        assertEquals("Test Subject", email.getSubject());
        assertEquals("Test Support <support@example.net>", email.getFromAddress().toString());
        email.buildMimeMessage();
        assertEquals("Test Message", email.getMimeMessage().getContent());
    }
}
