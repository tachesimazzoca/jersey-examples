package app.core;

import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

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
        if (httpOnly)
            this.comment = "HTTPOnly";
        else
            this.comment = "";
        this.maxAge = maxAge;
        this.secure = secure;
    }

    public CookieBaker create() {
        return new CookieBaker(secret, new NewCookie(
                cookieName, "", path, domain,
                version, comment, maxAge, secure));
    }

    public CookieBaker create(HttpHeaders headers) {
        Map<String, Cookie> cookies = headers.getCookies();
        if (cookies.containsKey(cookieName)) {
            return new CookieBaker(secret, new NewCookie(
                    cookieName, cookies.get(cookieName).getValue(),
                    path, domain, version, comment, maxAge, secure));
        } else {
            return create();
        }
    }
}
