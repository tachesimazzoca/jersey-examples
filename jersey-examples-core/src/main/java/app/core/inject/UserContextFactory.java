package app.core.inject;

import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;

public abstract class UserContextFactory<T> extends AbstractContainerRequestValueFactory<T> {

    public abstract UserContextFactory clone();

    public abstract Class<T> getGeneratedClass();
}

