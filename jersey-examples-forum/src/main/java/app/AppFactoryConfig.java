package app;

import app.mail.TextMailerFactory;

public class AppFactoryConfig {
    private TextMailerFactory signupMailerFactory;
    private TextMailerFactory recoveryMailerFactory;
    private TextMailerFactory profileMailerFactory;

    public TextMailerFactory getSignupMailerFactory() {
        return signupMailerFactory;
    }

    public void setSignupMailerFactory(TextMailerFactory signupMailerFactory) {
        this.signupMailerFactory = signupMailerFactory;
    }

    public TextMailerFactory getRecoveryMailerFactory() {
        return recoveryMailerFactory;
    }

    public void setRecoveryMailerFactory(TextMailerFactory recoveryMailerFactory) {
        this.recoveryMailerFactory = recoveryMailerFactory;
    }

    public TextMailerFactory getProfileMailerFactory() {
        return profileMailerFactory;
    }

    public void setProfileMailerFactory(TextMailerFactory profileMailerFactory) {
        this.profileMailerFactory = profileMailerFactory;
    }
}
