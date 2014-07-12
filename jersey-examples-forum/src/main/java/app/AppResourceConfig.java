package app;

import com.sun.jersey.api.core.ScanningResourceConfig;

import com.google.common.collect.ImmutableMap;

import app.core.*;
import app.controllers.*;
import app.renderer.*;

public class AppResourceConfig extends ScanningResourceConfig {
    public AppResourceConfig() {
        // config
        Configuration config = TypesafeConfig.load();

        // renderer
        String templateDir = this.getClass()
                .getResource("/views/freemarker").getPath();
        ImmutableMap<String, Object> sharedVariables =
                ImmutableMap.<String, Object> of("config", config);
        Renderer renderer = new FreemarkerRenderer(templateDir, sharedVariables);

        // providers
        getSingletons().add(new ViewMessageBodyWriter(renderer));

        // controllers
        getSingletons().add(new PagesController());
        getSingletons().add(new SignupController());
    }
}
