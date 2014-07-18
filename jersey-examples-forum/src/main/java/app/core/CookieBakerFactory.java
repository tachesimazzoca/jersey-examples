package app.core;

import javax.ws.rs.core.NewCookie;
import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Optional;

public class CookieBakerFactory {
    private final Optional<String> secret;
    private final String cookieName;
    private final String path;
    private final String domain;
    private final int version;
    private final String comment;
    private final int maxAge;
    private final boolean secure;

    public CookieBakerFactory(String cookieName) {
        this.secret = Optional.absent();
        this.cookieName = cookieName;
        this.path = "/";
        this.domain = "";
        this.version = NewCookie.DEFAULT_VERSION;
        this.comment = "";
        this.maxAge = NewCookie.DEFAULT_MAX_AGE;
        this.secure = false;
    }

    public CookieBakerFactory(Optional<String> secret, String cookieName) {
        this.secret = secret;
        this.cookieName = cookieName;
        this.path = "/";
        this.domain = "";
        this.version = NewCookie.DEFAULT_VERSION;
        this.comment = "";
        this.maxAge = NewCookie.DEFAULT_MAX_AGE;
        this.secure = false;
    }

    public CookieBakerFactory(
            Optional<String> secret,
            String cookieName,
            String path,
            String domain,
            int version,
            boolean httpOnly,
            int maxAge,
            boolean secure) {
        this.secret = secret;
        this.cookieName = cookieName;
        this.path = path;
        this.domain = domain;
        this.version = version;
        this.comment = "HTTPOnly";
        this.maxAge = maxAge;
        this.secure = secure;
    }

    public CookieBaker create() {
        return new CookieBaker(secret, new NewCookie(
                cookieName, "", path, domain,
                version, comment, maxAge, secure));
    }

    public CookieBaker create(HttpServletRequest req) {
        if (cookieName.isEmpty())
            throw new IllegalArgumentException(
                    "The field cookieName must be not empty");

        javax.servlet.http.Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(cookieName)) {
                    return new CookieBaker(secret, new NewCookie(
                            cookieName, cookies[i].getValue(),
                            path, domain, version, comment, maxAge, secure));
                }
            }
        }
        return create();
    }
}
