package app.core;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class View {
    private final String template;
    private final Map<String, Object> attributes;

    public View(String template) {
        this.template = template;
        this.attributes = ImmutableMap.of();
    }

    public View(String template, Map<String, Object> attributes) {
        this.template = template;
        this.attributes = attributes;
    }

    public String getTemplate() {
        return template;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
