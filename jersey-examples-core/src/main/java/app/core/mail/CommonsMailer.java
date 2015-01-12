package app.core.mail;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;

public class CommonsMailer implements Mailer {
    private final Email email;

    public CommonsMailer(Email email) {
        this.email = email;
    }

    public Email getEmail() {
        return email;
    }

    @Override
    public void send() throws MailerException {
        try {
            email.send();
        } catch (EmailException e) {
            throw new MailerException(e);
        }
    }
}
