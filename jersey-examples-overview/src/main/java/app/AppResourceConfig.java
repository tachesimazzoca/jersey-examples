package app;

import app.controllers.PagesController;
import app.core.config.Config;
import app.core.config.ConfigBinder;
import app.core.config.TypesafeConfig;
import app.core.inject.UserContextFactoryMap;
import app.core.inject.UserContextFactoryProvider;
import app.core.session.*;
import app.core.storage.FakeStorage;
import app.core.view.FreemarkerRenderer;
import app.core.view.Renderer;
import app.core.view.ViewMessageBodyWriter;
import app.resources.MainResource;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.Date;
import java.util.Map;

public class AppResourceConfig extends ResourceConfig {

    public AppResourceConfig() {
        // Config
        Config config = TypesafeConfig.load("conf/application");
        register(new ConfigBinder(config));

        // UserContext
        UserContextFactoryMap factoryMap = new UserContextFactoryMap(
            new StorageSessionFactory(
                new FakeStorage<Map<String, Object>>(), "APP_SESSION"),
            new CookieSessionFactory("APP_SESSION_ID", "aSecretKeyMustBeSpecified")
        );
        register(new UserContextFactoryProvider.Binder(factoryMap));

        // renderer
        String templateDir = this.getClass().getResource("/views/freemarker").getPath();
        Renderer renderer = new FreemarkerRenderer(templateDir);

        // MessageBodyWriters
        register(new ViewMessageBodyWriter(renderer));

        register(new MainResource(
                "Hello World! This application was registered at " + new Date()));
        register(new PagesController());

    }
}
