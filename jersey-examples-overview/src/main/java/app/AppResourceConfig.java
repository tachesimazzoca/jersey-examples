package app;

import app.controllers.PagesController;
import app.core.config.Config;
import app.core.config.ConfigBinder;
import app.core.config.TypesafeConfig;
import app.core.inject.UserContextFactoryMap;
import app.core.inject.UserContextFactoryProvider;
import app.core.session.CookieSessionFactory;
import app.core.session.StorageSessionFactory;
import app.core.storage.FakeStorage;
import app.core.view.FreemarkerRenderer;
import app.core.view.Renderer;
import app.core.view.ViewMessageBodyWriter;
import app.resources.MainResource;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.Date;
import java.util.Map;

import static app.core.util.ParameterUtils.params;

public class AppResourceConfig extends ResourceConfig {

    public AppResourceConfig() {
        // Config
        Config config = TypesafeConfig.load("/conf/application.conf");
        register(new ConfigBinder(config));

        // renderer
        String templateDir = this.getClass().getResource("/views/freemarker").getPath();
        Map<String, Object> sharedVariables = params("config", config);
        Renderer renderer = new FreemarkerRenderer(templateDir, sharedVariables);
        register(new ViewMessageBodyWriter(renderer));

        // UserContext
        UserContextFactoryMap factoryMap = new UserContextFactoryMap(
                new StorageSessionFactory(
                        new FakeStorage<Map<String, Object>>(), "APP_SESSION"),
                new CookieSessionFactory("APP_SESSION_ID", "aSecretKeyMustBeSpecified")
        );
        register(new UserContextFactoryProvider.Binder(factoryMap));

        register(new MainResource(
                "Hello World! This application was registered at " + new Date()));
        register(new PagesController());
    }
}
