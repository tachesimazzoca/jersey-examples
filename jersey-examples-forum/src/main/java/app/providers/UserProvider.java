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

import com.google.common.base.Optional;

import app.core.*;
import app.models.*;

@Provider
public class UserProvider implements InjectableProvider<Context, Type> {
    private final CookieBakerFactory sessionCookieFactory;
    private final AccountDao accountDao;

    public UserProvider(
            CookieBakerFactory sessionCookieFactory,
            AccountDao accountDao) {
        this.sessionCookieFactory = sessionCookieFactory;
        this.accountDao = accountDao;
    }

    @Override
    public Injectable<?> getInjectable(ComponentContext ic, Context ctx, Type t) {
        if (!t.equals(User.class)) {
            return null;
        }
        return new AbstractHttpContextInjectable<User>() {
            @Override
            public User getValue(HttpContext ctx) {
                CookieBaker session = sessionCookieFactory.create(ctx.getRequest());
                if (!session.get("id").isPresent())
                    return new User();
                String accountId = session.get("id").get();
                Optional<Account> accountOpt = accountDao.find(Long.parseLong(accountId));
                if (!accountOpt.isPresent())
                    return new User();
                else
                    return new User(accountOpt.get());
            }
        };
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }
}
