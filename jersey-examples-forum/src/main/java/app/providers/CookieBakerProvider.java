package app.providers;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import java.lang.reflect.Type;

import app.core.CookieBaker;
import app.core.CookieBakerFactory;

@Provider
public class CookieBakerProvider implements InjectableProvider<Context, Type> {
    private final CookieBakerFactory cookieBakerFactory;

    public CookieBakerProvider(CookieBakerFactory cookieBakerFactory) {
        this.cookieBakerFactory = cookieBakerFactory;
    }

    @Override
    public Injectable<?> getInjectable(ComponentContext ic, Context a, Type t) {
        if (!t.equals(CookieBaker.class)) {
            return null;
        }
        return new AbstractHttpContextInjectable<CookieBaker>() {
            @Override
            public CookieBaker getValue(HttpContext ctx) {
                return cookieBakerFactory.create(ctx.getRequest());
            }
        };
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }
}
