package app.core;

import java.util.Map;

import com.google.common.base.Optional;
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

    public static class Builder {
        private Optional<String> template;
        private ImmutableMap.Builder<String, Object> attributeBuilder;

        private Builder() {
            template = Optional.absent();
            attributeBuilder = new ImmutableMap.Builder<String, Object>();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public View build() throws IllegalArgumentException {
            if (!template.isPresent())
                throw new IllegalArgumentException("template must be not null");
            return new View(template.get(), attributeBuilder.build());
        }

        public Builder template(String template) {
            this.template = Optional.fromNullable(template);
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            attributeBuilder.putAll(attributes);
            return this;
        }

        public Builder attribute(String key, Object value) {
            attributeBuilder.put(key, value);
            return this;
        }
    }
}
