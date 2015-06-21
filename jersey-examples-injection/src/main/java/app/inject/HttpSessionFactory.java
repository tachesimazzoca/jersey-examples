package app.inject;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class HttpSessionFactory implements Factory<HttpSession> {
    private final HttpServletRequest request;

    @Inject
    public HttpSessionFactory(HttpServletRequest request) {
        System.out.println("HttpSessionFactory");
        this.request = request;
    }

    @Override
    public HttpSession provide() {
        System.out.println("HttpSessionFactory#provide");
        return request.getSession();
    }

    @Override
    public void dispose(HttpSession httpSession) {
        System.out.println("HttpSessionFactory#dispose");
    }

    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bindFactory(HttpSessionFactory.class).to(HttpSession.class);
        }
    }
}
