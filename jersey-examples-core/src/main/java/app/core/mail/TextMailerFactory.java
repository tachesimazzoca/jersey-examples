package app.core.mail;

import org.apache.commons.mail.SimpleEmail;

import javax.mail.internet.InternetAddress;
import java.util.HashSet;
import java.util.Set;

public class TextMailerFactory {
    private boolean fake = true;
    private String hostName = "localhost";
    private int smtpPort = 25;
    private String charset = "ISO-2022-JP";
    private String subject = "";
    private InternetAddress from;

    public void setFake(boolean fake) {
        this.fake = fake;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setFrom(InternetAddress from) {
        this.from = from;
    }

    public Mailer create(Set<InternetAddress> to, String msg) {
        try {
            SimpleEmail email;
            if (fake)
                email = new FakeSimpleEmail();
            else
                email = new SimpleEmail();
            email.setHostName(hostName);
            email.setSmtpPort(smtpPort);
            email.setCharset(charset);
            email.setSubject(subject);
            email.setFrom(from.getAddress(), from.getPersonal());
            email.setTo(to);
            email.setMsg(msg);
            return new CommonsMailer(email);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Mailer create(String to, String msg) {
        try {
            HashSet<InternetAddress> recipients = new HashSet<InternetAddress>();
            recipients.add(new InternetAddress(to));
            return create(recipients, msg);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
