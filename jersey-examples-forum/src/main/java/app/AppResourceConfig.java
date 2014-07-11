package app;

import com.sun.jersey.api.core.ScanningResourceConfig;

import app.controllers.*;
import app.core.*;
import app.renderer.FreemarkerRenderer;

import com.google.common.collect.ImmutableMap;

public class AppResourceConfig extends ScanningResourceConfig {
    public AppResourceConfig() {
        // PageMessageBodyWriter
        String templateDir = this.getClass()
                .getResource("/views/freemarker").getPath();
        getSingletons().add(
                new PageMessageBodyWriter(new FreemarkerRenderer(templateDir)));

        // PageController
        ImmutableMap.Builder<String, Object> configBuilder =
                new ImmutableMap.Builder<String, Object>();
        configBuilder.put("html", ImmutableMap.<String, String> of(
                "title", "<jersey-examples-forum>"));
        getSingletons().add(new PagesController(configBuilder.build()));
    }
}
