package app.core;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class TypesafeConfig implements Configuration {
    private final Config config;

    private TypesafeConfig(Config config) {
        this.config = config;
    }

    private TypesafeConfig(String name) {
        File f = new File(this.getClass().getResource(
                "/" + name + ".conf").getPath());
        this.config = ConfigFactory.parseFile(f);
    }

    public static TypesafeConfig load() {
        return new TypesafeConfig(ConfigFactory.load());
    }

    public static TypesafeConfig load(String name) {
        return new TypesafeConfig(name);
    }

    public static TypesafeConfig parseFile(File path) {
        return new TypesafeConfig(ConfigFactory.parseFile(path));
    }

    public static TypesafeConfig parseString(String conf) {
        return new TypesafeConfig(ConfigFactory.parseString(conf));
    }

    public Object get(String key) {
        return config.getAnyRef(key);
    }
}
