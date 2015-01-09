package app.core.view;

import java.util.Map;

public class View {
    private final String template;
    private final Map<String, Object> attributes;

    public View(String template) {
        this.template = template;
        this.attributes = null;
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
