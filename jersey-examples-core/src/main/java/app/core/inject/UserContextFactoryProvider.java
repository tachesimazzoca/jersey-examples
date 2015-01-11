package app.core.inject;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserContextFactoryProvider extends AbstractValueFactoryProvider {
    private UserContextFactoryMap factoryMap;

    @Inject
    public UserContextFactoryProvider(
            final MultivaluedParameterExtractorProvider extractorProvider,
            final ServiceLocator injector,
            final UserContextFactoryMap factoryMap
    ) {
        super(extractorProvider, injector, Parameter.Source.UNKNOWN);
        this.factoryMap = factoryMap;
    }

    @Override
    protected Factory<?> createValueFactory(Parameter parameter) {
        final Class<?> classType = parameter.getRawType();
        UserContext annotation = parameter.getAnnotation(UserContext.class);
        if (null == annotation)
            return null;

        UserContextFactory<?> factory = factoryMap.get(classType);
        if (null != factory)
            return factory.clone();
        else
            return null;
    }

    public static class SessionInjectionResolver extends ParamInjectionResolver<UserContext> {
        public SessionInjectionResolver() {
            super(UserContextFactoryProvider.class);
        }
    }

    public static class Binder extends AbstractBinder {
        private final UserContextFactoryMap factoryMap;

        public Binder(UserContextFactoryMap factoryMap) {
            this.factoryMap = factoryMap;
        }

        @Override
        protected void configure() {
            bind(this.factoryMap).to(UserContextFactoryMap.class);
            bind(UserContextFactoryProvider.class).to(ValueFactoryProvider.class)
                    .in(Singleton.class);
            bind(SessionInjectionResolver.class).to(
                    new TypeLiteral<InjectionResolver<UserContext>>() {
                    }
            ).in(Singleton.class);
        }
    }
}
