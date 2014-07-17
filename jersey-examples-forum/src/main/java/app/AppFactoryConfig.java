package app;

import app.models.SignupMailerFactory;

public class AppFactoryConfig {
    private SignupMailerFactory signupMailerFactory;

    public SignupMailerFactory getSignupMailerFactory() {
        return signupMailerFactory;
    }

    public void setSignupMailerFactory(SignupMailerFactory signupMailerFactory) {
        this.signupMailerFactory = signupMailerFactory;
    }
}
