package app;

import com.sun.jersey.api.core.ScanningResourceConfig;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import javax.persistence.EntityManagerFactory;

import java.io.IOException;
import java.util.Map;

import app.core.*;
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
        Storage signupStorage = new JPAStorage(ef, "signup_storage");
        Storage sessionStorage = new JPAStorage(ef, "session_storage");

        // session
        CookieBakerFactory sessionFactory = new CookieBakerFactory(
                config.maybe("app.secret", String.class),
                config.maybe("session.cookieName", String.class).or("APP_SESSION"));

        // dao
        UserDao userDao = new UserDaoImpl(ef);

        // renderer
        String templateDir = this.getClass()
                .getResource("/views/freemarker").getPath();
        Map<String, Object> sharedVariables = params("config", config);
        Renderer renderer = new FreemarkerRenderer(templateDir, sharedVariables);

        // providers
        getSingletons().add(new ViewMessageBodyWriter(renderer));

        // controllers
        getSingletons().add(new PagesController());
        getSingletons().add(new SignupController(
                signupStorage, userDao, factoryConfig.getSignupMailerFactory()));
        getSingletons().add(new AuthController(
                config, sessionFactory, sessionStorage, userDao));
    }
}
