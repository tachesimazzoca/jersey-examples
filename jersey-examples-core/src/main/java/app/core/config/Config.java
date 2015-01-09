package app.core.config;

import com.google.common.base.Optional;

public interface Config {

    Object get(String key);

    <T> T get(String key, Class<T> type);

    Optional<Object> maybe(String key);

    <T> Optional<T> maybe(String key, Class<T> type);
}
