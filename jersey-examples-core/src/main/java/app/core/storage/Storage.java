package app.core.storage;

import com.google.common.base.Optional;

public interface Storage<T> {

    String create(T value);

    Optional<T> read(String key);

    void write(String key, T value);

    void delete(String key);
}
