package app.renderer;

import java.io.OutputStream;
import java.util.Locale;

public interface Renderer {
    void render(String path, Object params, Locale locale, OutputStream os)
            throws Exception;
}
