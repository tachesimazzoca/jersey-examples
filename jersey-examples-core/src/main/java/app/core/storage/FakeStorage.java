package app.core.storage;

import com.google.common.base.Optional;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FakeStorage<T> implements Storage<T> {
    private static final int DEFAULT_MAX_ENTRIES = 10;

    private final ConcurrentMap<String, T> engine;
    private final int maxEntries;

    public FakeStorage() {
        this(DEFAULT_MAX_ENTRIES);
    }
    public FakeStorage(int maxEntries) {
        engine = new ConcurrentHashMap<String, T>();
        this.maxEntries = maxEntries;
    }

    @Override
    public String create(T value) {
        gc();
        String key = UUID.randomUUID().toString();
        engine.put(key, value);
        return key;
    }

    @Override
    public Optional<T> read(String key) {
        return Optional.fromNullable(engine.get(key));
    }

    @Override
    public void write(String key, T value) {
        gc();
        engine.put(key, value);
    }

    @Override
    public void delete(String key) {
        engine.remove(key);
    }

    public int size() {
        return engine.size();
    }

    private void gc() {
        int n = engine.size();
        if (n >= maxEntries) {
            String[] keys = engine.keySet().toArray(new String[n]);
            engine.remove(keys[new Random().nextInt(n)]);
        }
    }
}
