package app.core.mail;

public class MailerException extends Exception {
    public MailerException() {
    }

    public MailerException(String message) {
        super(message);
    }

    public MailerException(String message, Throwable cause) {
        super(message, cause);
    }

    public MailerException(Throwable cause) {
        super(cause);
    }

    public MailerException(String message, Throwable cause,
                           boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
