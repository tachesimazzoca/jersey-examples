package app.core.session;

import com.google.common.base.Optional;

import javax.ws.rs.core.*;

public interface Session {

    NewCookie toCookie();

    NewCookie toDiscardingCookie();

    Optional<String> get(String key);

    void put(String key, String value);

    Optional<String> remove(String key);

    void clear();

    boolean isEmpty();
}
