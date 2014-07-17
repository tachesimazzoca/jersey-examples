package app.mail;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;

public class CommonsMailer implements Mailer {
    private final Email email;

    public CommonsMailer(Email email) {
        this.email = email;
    }

    public void send() {
        try {
            email.send();
        } catch (EmailException e) {
            throw new RuntimeException(e);
        }
    }
}
