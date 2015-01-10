package app.core.session;

import com.google.common.base.Optional;

import java.util.HashMap;
import java.util.Map;

public class SessionFactoryMap {
    private Map<Class<?>, SessionFactory<?>> factoryMap;

    public SessionFactoryMap(SessionFactory<?> ...factories) {
        factoryMap = new HashMap<Class<?>, SessionFactory<?>>();
        for (SessionFactory<?> factory : factories) {
            factoryMap.put(factory.getGeneratedClass(), factory);
        }
    }

    public SessionFactory<?> get(Class<?> key) {
        return factoryMap.get(key);
    }
}
