package app.mail;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;

import java.util.logging.Logger;

public class CommonsMailer implements Mailer {
    private static final Logger LOGGER = Logger.getLogger(CommonsMailer.class.getName());

    private final Email email;
    private final boolean fake;

    public CommonsMailer(Email email, boolean fake) {
        this.email = email;
        this.fake = fake;
    }

    public void send() {
        try {
            if (fake)
                LOGGER.info((String) email.toString());
            else
                email.send();
        } catch (EmailException e) {
            throw new RuntimeException(e);
        }
    }
}
