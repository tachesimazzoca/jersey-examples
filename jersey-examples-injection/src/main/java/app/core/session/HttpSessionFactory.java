package app.core.session;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class HttpSessionFactory implements Factory<HttpSession> {
    private final HttpServletRequest request;

    @Inject
    public HttpSessionFactory(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public HttpSession provide() {
        return request.getSession();
    }

    @Override
    public void dispose(HttpSession t) {
    }

    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bindFactory(HttpSessionFactory.class).to(HttpSession.class);
        }
    }
}
