package app.core;

import com.google.common.base.Optional;

public interface Storage {
    public String create(Object value);

    public Optional<Object> read(String key);

    public <T> Optional<T> read(String key, Class<T> type);

    public void write(String key, Object value);

    public void delete(String key);
}
