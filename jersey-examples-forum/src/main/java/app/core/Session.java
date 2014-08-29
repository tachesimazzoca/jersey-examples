package app.core;

import javax.ws.rs.core.NewCookie;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Optional;

public class Session {
    private final Storage<Map<String, Object>> storage;
    private final NewCookie cookie;
    private final String sessionId;

    public Session(Storage<Map<String, Object>> storage, NewCookie cookie) {
        this(storage, cookie, null);
    }

    public Session(Storage<Map<String, Object>> storage, NewCookie cookie, String sessionId) {
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

    public void put(String key, Object value) {
        Optional<Map<String, Object>> opt = storage.read(sessionId);
        Map<String, Object> data;
        if (opt.isPresent()) {
            data = (Map<String, Object>) opt.get();
        } else {
            data = new HashMap<String, Object>();
        }
        data.put(key, value);
        storage.write(sessionId, data);
    }

    public Optional<String> remove(String key) {
        return remove(key, String.class);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> remove(String key, Class<T> type) {
        Optional<Map<String, Object>> opt = storage.read(sessionId);
        if (!opt.isPresent())
            return Optional.absent();
        Map<String, Object> data = opt.get();
        T v = null;
        if (data.containsKey(key))
            v = (T) data.get(key);
        data.remove(key);
        storage.write(sessionId, data);
        return Optional.fromNullable(v);
    }
}
