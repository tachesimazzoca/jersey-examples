package app.core.session;

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
public class SessionFactoryProvider extends AbstractValueFactoryProvider {
    private SessionFactoryMap sessionFactoryMap;

    @Inject
    public SessionFactoryProvider(
            final MultivaluedParameterExtractorProvider extractorProvider,
            final ServiceLocator injector,
            final SessionFactoryMap sessionFactoryMap
    ) {
        super(extractorProvider, injector, Parameter.Source.UNKNOWN);
        this.sessionFactoryMap = sessionFactoryMap;
    }

    @Override
    protected Factory<?> createValueFactory(Parameter parameter) {
        final Class<?> classType = parameter.getRawType();
        Session annotation = parameter.getAnnotation(Session.class);
        if (null == annotation)
            return null;

        SessionFactory<?> factory = sessionFactoryMap.get(classType);
        if (null != factory)
            return factory.clone();
        else
            return null;
    }

    public static class SessionInjectionResolver extends ParamInjectionResolver<Session> {
        public SessionInjectionResolver() {
            super(SessionFactoryProvider.class);
        }
    }

    public static class Binder extends AbstractBinder {
        private final SessionFactoryMap factoryMap;

        public Binder(SessionFactoryMap factoryMap) {
            this.factoryMap = factoryMap;
        }

        @Override
        protected void configure() {
            bind(this.factoryMap).to(SessionFactoryMap.class);
            bind(SessionFactoryProvider.class).to(ValueFactoryProvider.class)
                    .in(Singleton.class);
            bind(SessionInjectionResolver.class).to(
                    new TypeLiteral<InjectionResolver<Session>>() {
                    }
            ).in(Singleton.class);
        }
    }
}
