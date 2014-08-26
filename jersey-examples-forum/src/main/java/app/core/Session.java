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
        return get(key, String.class);
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        Optional<?> opt = storage.read(sessionId);
        if (!opt.isPresent()) {
            return Optional.absent();
        }
        @SuppressWarnings("unchecked")
        Map<String, T> m = (Map<String, T>) opt.get();
        if (!m.containsKey(key))
            return Optional.absent();
        return Optional.of(m.get(key));
    }

    @SuppressWarnings("unchecked")
    public <T> void put(String key, T value) {
        Optional<?> opt = storage.read(sessionId);
        Map<String, T> data;
        if (opt.isPresent()) {
            data = (Map<String, T>) opt.get();
        } else {
            data = new HashMap<String, T>();
        }
        data.put(key, value);
        storage.write(sessionId, data);
    }

    public Optional<String> remove(String key) {
        return remove(key, String.class);
    }

    public <T> Optional<T> remove(String key, Class<T> type) {
        Optional<?> opt = storage.read(sessionId);
        if (!opt.isPresent())
            return Optional.absent();
        @SuppressWarnings("unchecked")
        Map<String, T> data = (Map<String, T>) opt.get();
        T v = null;
        if (data.containsKey(key))
            v = data.get(key);
        data.remove(key);
        storage.write(sessionId, data);
        return Optional.fromNullable(v);
    }
}
