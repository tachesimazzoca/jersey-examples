package app;

import app.controllers.IndexController;
import app.inject.HttpSessionFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class AppResourceConfig extends ResourceConfig {
    public AppResourceConfig() {
        register(new IndexController());

        register(new HttpSessionFactory.Binder());
    }
}
