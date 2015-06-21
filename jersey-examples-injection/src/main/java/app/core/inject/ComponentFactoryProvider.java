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
public class ComponentFactoryProvider extends AbstractValueFactoryProvider {
    private ComponentFactoryMap factoryMap;

    @Inject
    public ComponentFactoryProvider(
            final MultivaluedParameterExtractorProvider extractorProvider,
            final ServiceLocator injector,
            final ComponentFactoryMap factoryMap
    ) {
        super(extractorProvider, injector, Parameter.Source.UNKNOWN);
        this.factoryMap = factoryMap;
    }

    @Override
    protected Factory<?> createValueFactory(Parameter parameter) {
        final Class<?> classType = parameter.getRawType();
        Component annotation = parameter.getAnnotation(Component.class);
        if (null == annotation)
            return null;

        ComponentFactory<?> factory = factoryMap.get(classType);
        if (null != factory)
            return factory.clone();
        else
            return null;
    }

    public static class ComponentInjectionResolver extends ParamInjectionResolver<Component> {
        public ComponentInjectionResolver() {
            super(ComponentFactoryProvider.class);
        }
    }

    public static class Binder extends AbstractBinder {
        private final ComponentFactoryMap factoryMap;

        public Binder(ComponentFactoryMap factoryMap) {
            this.factoryMap = factoryMap;
        }

        @Override
        protected void configure() {
            bind(this.factoryMap).to(ComponentFactoryMap.class);
            bind(ComponentFactoryProvider.class).to(ValueFactoryProvider.class)
                    .in(Singleton.class);
            bind(ComponentInjectionResolver.class).to(
                    new TypeLiteral<InjectionResolver<Component>>() {
                    }
            ).in(Singleton.class);
        }
    }
}
