package app.core;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import app.renderer.Renderer;

@Provider
@Produces(MediaType.WILDCARD)
public class ViewMessageBodyWriter implements MessageBodyWriter<View> {
    @Context
    private HttpHeaders headers;

    private static final Logger LOGGER = Logger
            .getLogger(ViewMessageBodyWriter.class.getName());

    private final Renderer renderer;

    public ViewMessageBodyWriter(Renderer renderer) {
        this(renderer, File.separator);
    }

    public ViewMessageBodyWriter(Renderer renderer, String directory) {
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
            renderer.render(t.getTemplate(), t.getAttributes(),
                    detectLocale(headers), entityStream);
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            throw new WebApplicationException(Response.serverError().build());
        }
    }

    private Locale detectLocale(HttpHeaders headers) {
        final List<Locale> languages = headers.getAcceptableLanguages();
        for (Locale locale : languages) {
            if (!locale.toString().contains("*")) {
                return locale;
            }
        }
        return Locale.getDefault();
    }
}
