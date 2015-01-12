package app.models;

import app.core.inject.UserContextFactory;
import app.core.session.StorageSession;
import app.core.storage.Storage;

import javax.ws.rs.core.*;
import java.util.Map;

public class UserHelperFactory extends UserContextFactory<UserHelper> {
    @Context
    HttpHeaders headers;

    private final AccountDao accountDao;
    private final Storage<Map<String, Object>> storage;
    private final String cookieName;
    private final String path;
    private final String domain;

    public UserHelperFactory(
            AccountDao accountDao,
            Storage<Map<String, Object>> storage,
            String cookieName) {
        this(accountDao, storage, cookieName, "/", "");
    }

    public UserHelperFactory(
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
    public UserContextFactory clone() {
        return new UserHelperFactory(accountDao, storage, cookieName, path, domain);
    }

    public void setHttpHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public Class<UserHelper> getGeneratedClass() {
        return UserHelper.class;
    }

    @Override
    public UserHelper provide() {
        String sessionId = null;
        Map<String, Cookie> cookies = headers.getCookies();
        if (cookies.containsKey(cookieName))
            sessionId = cookies.get(cookieName).getValue();
        NewCookie cookie = new NewCookie(
                cookieName, sessionId, path, domain,
                NewCookie.DEFAULT_VERSION, "",
                NewCookie.DEFAULT_MAX_AGE, false);
        return new UserHelper(new StorageSession(storage, cookie, sessionId), accountDao);
    }
}
