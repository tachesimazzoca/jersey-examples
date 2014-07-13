package app.core;

import com.google.common.base.Optional;

public interface Storage {
    public String create(String value);

    public Optional<String> read(String key);

    public void write(String key, String value);

    public void delete(String key);
}
