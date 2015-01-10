package app.core.session;

import java.util.Map;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

public class CookieSessionFactory extends SessionFactory<CookieSession> {
    @Context
    private HttpHeaders headers;

    private final NewCookie cookie;
    private final String secret;

    public CookieSessionFactory(NewCookie cookie, String secret) {
        this.cookie = cookie;
        this.secret = secret;
    }

    public CookieSessionFactory(String cookieName) {
        this(cookieName, null);
    }

    public CookieSessionFactory(String cookieName, String secret) {
        this(cookieName, "/", "", NewCookie.DEFAULT_VERSION, "",
                NewCookie.DEFAULT_MAX_AGE, false, secret);
    }

    public CookieSessionFactory(
            String cookieName,
            String path,
            String domain,
            int version,
            String comment,
            int maxAge,
            boolean secure,
            String secret) {
        this.cookie = new NewCookie(
                cookieName, "", path, domain,
                version, comment, maxAge, secure);
        this.secret = secret;
    }

    public void setHttpHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public CookieSessionFactory clone() {
        return new CookieSessionFactory(cookie, secret);
    }

    @Override
    public Class<CookieSession> getGeneratedClass() {
        return CookieSession.class;
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
        if (null == newCookie){
            newCookie = new NewCookie(
                    cookie.getName(), "",
                    cookie.getPath(), cookie.getDomain(),
                    cookie.getVersion(), cookie.getComment(),
                    cookie.getMaxAge(), cookie.isSecure());
        }
        return new CookieSession(newCookie, secret);
    }
}
