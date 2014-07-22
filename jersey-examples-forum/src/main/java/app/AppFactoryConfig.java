package app;

import app.models.TextMailerFactory;

public class AppFactoryConfig {
    private TextMailerFactory signupMailerFactory;
    private TextMailerFactory profileMailerFactory;

    public TextMailerFactory getSignupMailerFactory() {
        return signupMailerFactory;
    }

    public void setSignupMailerFactory(TextMailerFactory signupMailerFactory) {
        this.signupMailerFactory = signupMailerFactory;
    }

    public TextMailerFactory getProfileMailerFactory() {
        return profileMailerFactory;
    }

    public void setProfileMailerFactory(TextMailerFactory profileMailerFactory) {
        this.profileMailerFactory = profileMailerFactory;
    }
}
