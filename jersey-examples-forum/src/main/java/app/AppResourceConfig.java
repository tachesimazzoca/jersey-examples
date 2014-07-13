package app;

import com.sun.jersey.api.core.ScanningResourceConfig;

import javax.persistence.Persistence;
import javax.persistence.EntityManagerFactory;

import java.util.Map;

import app.core.*;
import app.controllers.*;
import app.renderer.*;

import static app.core.Util.params;

public class AppResourceConfig extends ScanningResourceConfig {
    public AppResourceConfig() {
        // config
        Configuration config = TypesafeConfig.load();

        // storage
        EntityManagerFactory ef =
                Persistence.createEntityManagerFactory("default");
        Storage signupStorage = new JPAStorage(ef, "signup_storage");
                
        // renderer
        String templateDir = this.getClass()
                .getResource("/views/freemarker").getPath();
        Map<String, Object> sharedVariables = params("config", config);
        Renderer renderer = new FreemarkerRenderer(templateDir, sharedVariables);

        // providers
        getSingletons().add(new ViewMessageBodyWriter(renderer));

        // controllers
        getSingletons().add(new PagesController());
        getSingletons().add(new SignupController(signupStorage));
    }
}
