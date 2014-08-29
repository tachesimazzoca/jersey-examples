package app.providers;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.Provider;

import java.lang.reflect.Type;
import java.util.Map;

import app.core.Storage;
import app.core.Session;
import app.models.AccountDao;
import app.models.UserContext;

@Provider
public class UserContextProvider implements InjectableProvider<Context, Type> {
    private final AccountDao accountDao;
    private final Storage<Map<String, Object>> storage;
    private final String cookieName;
    private final String path;
    private final String domain;

    public UserContextProvider(
            AccountDao accountDao,
            Storage<Map<String, Object>> storage,
            String cookieName) {
        this(accountDao, storage, cookieName, "/", "");
    }

    public UserContextProvider(
            AccountDao accountDao,
            Storage<Map<String, Object>> storage,
            String cookieName,
            String path,
            String domain) {
        this.accountDao = accountDao;
        this.storage = storage;
        this.cookieName = cookieName;
        this.path = path;
        this.domain = domain;
    }

    @Override
    public Injectable<?> getInjectable(ComponentContext ic, Context a, Type t) {
        if (!t.equals(UserContext.class)) {
            return null;
        }
        return new AbstractHttpContextInjectable<UserContext>() {
            @Override
            public UserContext getValue(HttpContext ctx) {
                String sessionId = null;
                Map<String, Cookie> cookies = ctx.getRequest().getCookies();
                if (cookies.containsKey(cookieName))
                    sessionId = cookies.get(cookieName).getValue();
                NewCookie cookie = new NewCookie(
                        cookieName, sessionId, path, domain,
                        NewCookie.DEFAULT_VERSION, "",
                        NewCookie.DEFAULT_MAX_AGE, false);
                return new UserContext(new Session(storage, cookie, sessionId), accountDao);
            }
        };
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }
}
