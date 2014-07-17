package app.core;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Optional;

import static app.core.Util.objectToBase64;
import static app.core.Util.base64ToObject;

public class Session {
    private static final int MAX_VALUE_LENGTH = 2048;
    private Cookie cookie;

    public Session(Cookie cookie) {
        this.cookie = cookie;
    }

    public NewCookie getNewCookie() {
        return new NewCookie(cookie);
    }

    @SuppressWarnings("unchecked")
    public Optional<Object> get(String key) {
        Optional<Object> v = null;
        if (!cookie.getValue().isEmpty()) {
            try {
                Map<String, Object> m = base64ToObject(cookie.getValue(), Map.class);
                if (m.containsKey(key))
                    v = Optional.of(m.get(key));
            } catch (IllegalArgumentException e) {
                // Ignore exception, because the cookie value might be corrupted.
                v = null;
            }
        }
        if (v == null)
            v = Optional.absent();
        return v;
    }

    @SuppressWarnings("unchecked")
    public Session put(String key, Object value) {
        Map<String, Object> m = null;
        if (!cookie.getValue().isEmpty()) {
            try {
                m = base64ToObject(cookie.getValue(), HashMap.class);
            } catch (IllegalArgumentException e) {
                // Ignore exception, because the cookie value might be corrupted.
                m = null;
            }
        }
        if (m == null)
            m = new HashMap<String, Object>();
        m.put(key, value);
        String v = objectToBase64(m);
        if (v.length() > MAX_VALUE_LENGTH)
            v = "";
        cookie = new Cookie(cookie.getName(), v, cookie.getPath(), cookie.getDomain());
        return this;
    }

    public Session clear() {
        cookie = new Cookie(cookie.getName(), "", cookie.getPath(), cookie.getDomain());
        return this;
    }
}
