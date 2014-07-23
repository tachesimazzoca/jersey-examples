package app.mail;

import org.apache.commons.mail.SimpleEmail;

import javax.mail.internet.InternetAddress;
import java.util.Set;

import com.google.common.collect.ImmutableSet;


public class TextMailerFactory {
    private boolean fake = true;
    private String hostName = "localhost";
    private int smtpPort = 25;
    private String subject = "";
    private InternetAddress from;

    public boolean isFake() {
        return fake;
    }

    public void setFake(boolean fake) {
        this.fake = fake;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public InternetAddress getFrom() {
        return from;
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
            email.setCharset("ISO-2022-JP");
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
            return create(ImmutableSet.of(new InternetAddress(to)), msg);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
