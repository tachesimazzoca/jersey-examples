package app.providers;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import java.lang.reflect.Type;

import app.core.Config;

@Provider
public class ConfigProvider implements InjectableProvider<Context, Type> {
    private final Config config;

    public ConfigProvider(Config config) {
        this.config = config;
    }

    @Override
    public Injectable<?> getInjectable(ComponentContext ic, Context a, Type t) {
        if (!t.equals(Config.class)) {
            return null;
        }
        return new Injectable<Config>() {
            @Override
            public Config getValue() {
                return config;
            }
        };
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.Singleton;
    }
}
