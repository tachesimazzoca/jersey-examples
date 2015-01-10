package app.core.session;

import app.core.storage.Storage;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import java.util.Map;

public class StorageSessionFactory extends SessionFactory<StorageSession> {
    @Context
    private HttpHeaders headers;

    private final Storage<Map<String, Object>> storage;
    private final String cookieName;
    private final String path;
    private final String domain;

    public StorageSessionFactory(
            Storage<Map<String, Object>> storage,
            String cookieName) {
        this(storage, cookieName, "/", "");
    }

    public StorageSessionFactory(
            Storage<Map<String, Object>> storage,
            String cookieName,
            String path,
            String domain) {
        this.storage = storage;
        this.cookieName = cookieName;
        this.path = path;
        this.domain = domain;
    }

    public void setHttpHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public StorageSessionFactory clone() {
        return new StorageSessionFactory(storage, cookieName, path, domain);
    }

    @Override
    public StorageSession provide() {
        Map<String, Cookie> cookies = headers.getCookies();
        String sessionId;
        if (cookies.containsKey(cookieName))
            sessionId = cookies.get(cookieName).getValue();
        else
            sessionId = null;
        NewCookie cookie = new NewCookie(
                cookieName, "", path, domain,
                NewCookie.DEFAULT_VERSION, "",
                NewCookie.DEFAULT_MAX_AGE, false);
        return new StorageSession(storage, cookie, sessionId);
    }

    @Override
    public Class<StorageSession> getGeneratedClass() {
        return StorageSession.class;
    }
}
