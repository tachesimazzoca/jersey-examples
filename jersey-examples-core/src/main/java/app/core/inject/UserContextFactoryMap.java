package app.core.inject;

import java.util.HashMap;
import java.util.Map;

public class UserContextFactoryMap {
    private Map<Class<?>, UserContextFactory<?>> factoryMap;

    public UserContextFactoryMap(UserContextFactory<?>... factories) {
        factoryMap = new HashMap<Class<?>, UserContextFactory<?>>();
        for (UserContextFactory<?> factory : factories) {
            factoryMap.put(factory.getGeneratedClass(), factory);
        }
    }

    public UserContextFactory<?> get(Class<?> key) {
        return factoryMap.get(key);
    }
}
