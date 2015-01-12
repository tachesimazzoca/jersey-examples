package app;

import app.controllers.*;
import app.core.config.Config;
import app.core.config.ConfigBinder;
import app.core.config.TypesafeConfig;
import app.core.inject.UserContextFactoryMap;
import app.core.inject.UserContextFactoryProvider;
import app.core.storage.Storage;
import app.core.util.FileHelper;
import app.core.util.Jackson;
import app.core.view.FreemarkerRenderer;
import app.core.view.Renderer;
import app.core.view.ViewMessageBodyWriter;
import app.db.JPAStorage;
import app.mail.TextMailerFactory;
import app.models.*;
import app.resources.UploadResource;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.util.Map;

import static app.core.util.ParameterUtils.params;

public class AppResourceConfig extends ResourceConfig {
    public AppResourceConfig() throws IOException {
        // features
        register(MultiPartFeature.class);

        // factory
        AppFactoryConfig factoryConfig = Jackson.fromYAML(
                getClass().getResourceAsStream("/conf/factory.yml"),
                AppFactoryConfig.class);

        // config
        Config config = TypesafeConfig.load("/conf/application.conf");
        register(new ConfigBinder(config));

        // storage
        EntityManagerFactory ef = Persistence.createEntityManagerFactory("default");
        Storage<Map<String, Object>> userStorage =
                new JPAStorage(ef, "session_storage", "user-");
        Storage<Map<String, Object>> signupStorage =
                new JPAStorage(ef, "session_storage", "signup-");
        Storage<Map<String, Object>> recoveryStorage =
                new JPAStorage(ef, "session_storage", "recovery-");
        Storage<Map<String, Object>> profileStorage =
                new JPAStorage(ef, "session_storage", "profile-");

        // dao
        AccountDao accountDao = new AccountDao(ef);
        QuestionDao questionDao = new QuestionDao(ef);
        AnswerDao answerDao = new AnswerDao(ef);
        AccountQuestionDao accountQuestionDao = new AccountQuestionDao(ef);
        AccountAnswerDao accountAnswerDao = new AccountAnswerDao(ef);

        // mailer
        TextMailerFactory signupMailerFactory = factoryConfig.getSignupMailerFactory();
        TextMailerFactory recoveryMailerFactory = factoryConfig.getRecoveryMailerFactory();
        TextMailerFactory profileMailerFactory = factoryConfig.getProfileMailerFactory();

        // renderer
        String templateDir = this.getClass().getResource("/views/freemarker").getPath();
        Map<String, Object> sharedVariables = params("config", config);
        Renderer renderer = new FreemarkerRenderer(templateDir, sharedVariables);
        register(new ViewMessageBodyWriter(renderer));

        // providers
        UserContextFactoryMap factoryMap = new UserContextFactoryMap(
                new ForumUserFactory(accountDao, userStorage, "APP_SESSION"));
        register(new UserContextFactoryProvider.Binder(factoryMap));

        // finder
        String tmpPath = config.get("path.tmp", String.class);
        TempFileHelper tempFileHelper = new TempFileHelper(tmpPath);
        String uploadPath = config.get("path.upload", String.class);
        FileHelper accountsIconFinder = FileHelperFactory.createAccountsIconFinder(
            uploadPath + "/accounts/icon");

        // resources
        register(new UploadResource(tempFileHelper, accountsIconFinder));

        // controllers
        register(new PagesController());
        register(new AccountsController(accountDao, signupStorage, signupMailerFactory));
        register(new RecoveryController(accountDao, recoveryStorage, recoveryMailerFactory));
        register(new DashboardController(questionDao, answerDao));
        register(new ProfileController(accountDao, tempFileHelper, accountsIconFinder,
                profileStorage, profileMailerFactory));
        register(new QuestionsController(questionDao, answerDao, accountDao, accountQuestionDao));
        register(new AnswersController(questionDao, answerDao, accountAnswerDao));
    }
}
