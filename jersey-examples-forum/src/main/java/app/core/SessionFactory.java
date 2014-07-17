package app.core;

import javax.ws.rs.core.Cookie;
import javax.servlet.http.HttpServletRequest;

public class SessionFactory {
    private String cookieName = "";
    private String path = "/";
    private String domain = "";

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Session create(HttpServletRequest req) {
        if (cookieName.isEmpty())
            throw new IllegalArgumentException(
                    "The field cookieName must be not empty");

        javax.servlet.http.Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(cookieName)) {
                    return new Session(new Cookie(cookieName,
                            cookies[i].getValue(), path, domain));
                }
            }
        }
        return new Session(new Cookie(cookieName, "", path, domain));
    }
}
