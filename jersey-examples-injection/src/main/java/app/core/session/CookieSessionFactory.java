package app.core.session;

import app.core.inject.ComponentFactory;

import javax.ws.rs.core.*;
import java.util.Map;

public class CookieSessionFactory extends ComponentFactory<CookieSession> {
    @Context
    HttpHeaders headers;

    private final NewCookie cookie;
    private final String secret;

    public CookieSessionFactory(NewCookie cookie) {
        this(cookie, null);
    }

    public CookieSessionFactory(NewCookie cookie, String secret) {
        this.cookie = cookie;
        this.secret = secret;
    }

    @Override
    public CookieSession provide() {
        NewCookie newCookie = null;
        if (null != headers) {
            Map<String, Cookie> cookies = headers.getCookies();
            String key = cookie.getName();
            if (cookies.containsKey(key)) {
                newCookie = new NewCookie(
                        cookie.getName(), cookies.get(key).getValue(),
                        cookie.getPath(), cookie.getDomain(),
                        cookie.getVersion(), cookie.getComment(),
                        cookie.getMaxAge(), cookie.isSecure());
            }
        }
        if (null == newCookie) {
            newCookie = new NewCookie(
                    cookie.getName(), "",
                    cookie.getPath(), cookie.getDomain(),
                    cookie.getVersion(), cookie.getComment(),
                    cookie.getMaxAge(), cookie.isSecure());
        }
        return new CookieSession(newCookie, secret);
    }

    @Override
    public void dispose(CookieSession t) {
    }

    @Override
    public ComponentFactory clone() {
        return new CookieSessionFactory(cookie, secret);
    }

    @Override
    public Class<?> getGeneratedClass() {
        return Session.class;
    }
}
