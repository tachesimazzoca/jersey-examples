package app;

import com.sun.jersey.api.core.ScanningResourceConfig;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import javax.persistence.EntityManagerFactory;

import java.io.IOException;
import java.util.Map;

import app.core.*;
import app.providers.*;
import app.mail.TextMailerFactory;
import app.models.*;
import app.controllers.*;
import app.renderer.*;

import static app.core.Util.params;

public class AppResourceConfig extends ScanningResourceConfig {
    public AppResourceConfig() throws IOException {
        // factory
        JsonParser parser = new YAMLFactory().createParser(
                this.getClass().getResourceAsStream("/conf/factory.yml"));
        ObjectMapper mapper = Jackson.newObjectMapper();
        AppFactoryConfig factoryConfig = mapper.readValue(parser, AppFactoryConfig.class);

        // config
        Config config = Config.load("conf/application");

        // storage
        EntityManagerFactory ef = JPA.ef();
        Storage userStorage = new JPAStorage(ef, "session_storage", "user-");
        Storage signupStorage = new JPAStorage(ef, "session_storage", "signup-");
        Storage recoveryStorage = new JPAStorage(ef, "session_storage", "recovery-");
        Storage profileStorage = new JPAStorage(ef, "session_storage", "profile-");

        // dao
        AccountDao accountDao = new AccountDao(ef);
        QuestionDao questionDao = new QuestionDao(ef);
        AnswerDao answerDao = new AnswerDao(ef);
        // AccountQuestionDao accountQuestionDao = new AccountQuestionDao(ef);

        // mailer
        TextMailerFactory signupMailerFactory = factoryConfig.getSignupMailerFactory();
        TextMailerFactory recoveryMailerFactory = factoryConfig.getRecoveryMailerFactory();
        TextMailerFactory profileMailerFactory = factoryConfig.getProfileMailerFactory();

        // renderer
        String templateDir = this.getClass()
                .getResource("/views/freemarker").getPath();
        Map<String, Object> sharedVariables = params("config", config);
        Renderer renderer = new FreemarkerRenderer(templateDir, sharedVariables);

        // providers
        getSingletons().add(new ViewMessageBodyWriter(renderer));
        getSingletons().add(new SessionProvider(userStorage, "APP_SESSION"));

        // controllers
        getSingletons().add(new PagesController());
        getSingletons().add(new AccountsController(
                accountDao, signupStorage, signupMailerFactory));
        getSingletons().add(new RecoveryController(
                accountDao, recoveryStorage, recoveryMailerFactory));
        getSingletons().add(new DashboardController(accountDao, questionDao, answerDao));
        getSingletons().add(new ProfileController(
                accountDao, profileStorage, profileMailerFactory));
        getSingletons().add(new QuestionsController(accountDao, questionDao, answerDao));
        getSingletons().add(new AnswersController(accountDao, questionDao, answerDao));
    }
}
