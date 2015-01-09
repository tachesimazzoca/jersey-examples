package app.core.config;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class ConfigBinder extends AbstractBinder {
    private final ConfigFactory factory;

    public static class ConfigFactory implements Factory<Config> {

        private final Config config;

        public ConfigFactory(Config config) {
            this.config = config;
        }

        @Override
        public Config provide() {
            return config;
        }

        @Override
        public void dispose(Config o) {
        }
    }

    public ConfigBinder(Config config) {
        this.factory = new ConfigFactory(config);
    }

    @Override
    protected void configure() {
        bindFactory(factory).to(Config.class);
    }
}
