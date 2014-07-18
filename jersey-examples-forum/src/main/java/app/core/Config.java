package app.core;

import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigException;

import com.google.common.base.Optional;

import java.io.File;

public class Config {
    private final com.typesafe.config.Config typesafeConfig;

    private Config(com.typesafe.config.Config typesafeConfig) {
        this.typesafeConfig = typesafeConfig;
    }

    private Config(String name) {
        File f = new File(this.getClass().getResource(
                "/" + name + ".conf").getPath());
        this.typesafeConfig = ConfigFactory.parseFile(f);
    }

    public static Config load() {
        return new Config(ConfigFactory.load());
    }

    public static Config load(String name) {
        return new Config(name);
    }

    public static Config parseFile(File path) {
        return new Config(ConfigFactory.parseFile(path));
    }

    public static Config parseString(String conf) {
        return new Config(ConfigFactory.parseString(conf));
    }

    public Object get(String key) {
        return typesafeConfig.getAnyRef(key);
    }

    public Optional<Object> maybe(String key) {
        Optional<Object> o;
        try {
            Object obj = typesafeConfig.getAnyRef(key);
            o = Optional.of(obj);
        } catch (ConfigException.Missing e) {
            o = Optional.absent();
        }
        return o;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> maybe(String key, Class<T> type) {
        Optional<T> o;
        try {
            T obj = (T) typesafeConfig.getAnyRef(key);
            o = Optional.of(obj);
        } catch (ConfigException.Missing e) {
            o = Optional.absent();
        }
        return o;
    }
}
