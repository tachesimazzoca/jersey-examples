package app.core;

import javax.ws.rs.core.NewCookie;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Optional;

public class Session {
    private final Storage storage;
    private final NewCookie cookie;
    private final String sessionId;

    public Session(Storage storage, NewCookie cookie) {
        this(storage, cookie, null);
    }

    public Session(Storage storage, NewCookie cookie, String sessionId) {
        this.storage = storage;
        this.cookie = cookie;
        if (sessionId == null || sessionId.isEmpty()
                || !storage.read(sessionId).isPresent()) {
            this.sessionId = this.storage.create(null);
        } else {
            this.sessionId = sessionId;
        }
    }

    public NewCookie toCookie() {
        return new NewCookie(cookie.getName(), sessionId,
                cookie.getPath(), cookie.getDomain(),
                cookie.getVersion(), cookie.getComment(),
                cookie.getMaxAge(), cookie.isSecure());
    }

    public Optional<String> get(String key) {
        Optional<?> opt = storage.read(sessionId);
        if (!opt.isPresent()) {
            return Optional.absent();
        }
        @SuppressWarnings("unchecked")
        Map<String, String> m = (Map<String, String>) opt.get();
        if (!m.containsKey(key))
            return Optional.absent();
        return Optional.of(m.get(key));
    }

    @SuppressWarnings("unchecked")
    public void put(String key, String value) {
        Optional<?> opt = storage.read(sessionId);
        Map<String, String> data;
        if (opt.isPresent()) {
            data = (Map<String, String>) opt.get();
        } else {
            data = new HashMap<String, String>();
        }
        data.put(key, value);
        storage.write(sessionId, data);
    }

    public Optional<String> remove(String key) {
        Optional<?> opt = storage.read(sessionId);
        if (!opt.isPresent())
            return Optional.absent();
        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) opt.get();
        String v = null;
        if (data.containsKey(key))
            v = data.get(key);
        data.remove(key);
        storage.write(sessionId, data);
        return Optional.fromNullable(v);
    }
}
