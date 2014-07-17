package app.models;

import javax.mail.internet.InternetAddress;

import com.google.common.collect.ImmutableSet;

import org.apache.commons.mail.SimpleEmail;

import app.mail.CommonsMailer;
import app.mail.FakeSimpleEmail;
import app.mail.Mailer;

public class SignupMailerFactory {
    private boolean fake = true;
    private String hostName = "localhost";
    private int smtpPort = 25;
    private String subject = "Jersey Examples Forum";
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

    public Mailer create(String to, String url) {
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
            email.setTo(ImmutableSet.of(new InternetAddress(to)));
            email.setMsg(url);
            return new CommonsMailer(email);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
