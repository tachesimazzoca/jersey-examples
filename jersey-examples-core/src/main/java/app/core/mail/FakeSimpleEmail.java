package app.core.mail;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import java.util.logging.Logger;

public class FakeSimpleEmail extends SimpleEmail {
    private static final Logger LOGGER = Logger.getLogger(FakeSimpleEmail.class.getName());

    @Override
    public String sendMimeMessage() throws EmailException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String send() throws EmailException {
        LOGGER.info(this.content.toString());
        return "";
    }
}
