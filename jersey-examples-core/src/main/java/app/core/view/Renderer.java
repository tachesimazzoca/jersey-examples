package app.core.view;

import java.io.OutputStream;

public interface Renderer {
    void render(String path, Object params, OutputStream os) throws Exception;
}
