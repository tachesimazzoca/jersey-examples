package app.core.mail;

public interface Mailer {
    void send() throws MailerException;
}
