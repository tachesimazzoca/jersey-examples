package app.core.session;

import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;

public abstract class SessionFactory<T> extends AbstractContainerRequestValueFactory<T> {

    public abstract SessionFactory clone();

    public abstract Class<T> getGeneratedClass();
}
