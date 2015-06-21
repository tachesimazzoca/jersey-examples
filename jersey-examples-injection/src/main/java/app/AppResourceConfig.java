package app;

import app.controllers.IndexController;
import app.core.inject.ComponentFactoryMap;
import app.core.inject.ComponentFactoryProvider;
import app.core.session.CookieSessionFactory;
import app.core.session.HttpSessionFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.*;

public class AppResourceConfig extends ResourceConfig {
    public AppResourceConfig() {
        register(new IndexController());

        register(new HttpSessionFactory.Binder());

        CookieSessionFactory cookieSessionFactory = new CookieSessionFactory(
                new NewCookie("APP_SESSION", null), "secretkey");
        register(new ComponentFactoryProvider.Binder(
                new ComponentFactoryMap(cookieSessionFactory)));
    }
}
