package app.core.view;


import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Logger;

@Provider
@Produces(MediaType.WILDCARD)
public class ViewMessageBodyWriter implements MessageBodyWriter<View> {
    private static final Logger LOGGER = Logger
            .getLogger(ViewMessageBodyWriter.class.getName());

    private final Renderer renderer;

    public ViewMessageBodyWriter(Renderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
                               Annotation[] annotations, MediaType mediaType) {
        return View.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(View t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType) {
        // deprecated by JAX-RS 2.0 and ignored by Jersey runtime
        return -1;
    }

    @Override
    public void writeTo(View t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException,
            WebApplicationException {
        try {
            renderer.render(t.getTemplate(), t.getAttributes(), entityStream);
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            throw new WebApplicationException(Response.serverError().build());
        }
    }
}
