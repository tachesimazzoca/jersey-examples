package app.renderer;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import com.google.common.collect.ImmutableMap;

public class FreemarkerRendererTest {
    @Test(expected = IOException.class)
    public void testNoTemplate() throws Exception {
        Renderer renderer = new FreemarkerRenderer();
        OutputStream os = new ByteArrayOutputStream();
        renderer.render("notemplate.ftl",
                ImmutableMap.<String, Object> of(),
                Locale.US, os);
    }

    private String renderHello(String path) throws Exception {
        Renderer renderer = new FreemarkerRenderer();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        renderer.render(path,
                ImmutableMap.<String, Object> of("name", "World!"),
                Locale.US, os);
        return new String(os.toByteArray(), "UTF-8");
    }

    @Test
    public void testRelativeTemplatePath() throws Exception {
        assertEquals("Hello World!", renderHello("index.ftl"));
    }

    @Test
    public void testAbsoluteTemplatePath() throws Exception {
        String path = this.getClass()
                .getResource("/views/freemarker/index.ftl").getPath();
        assertEquals("Hello World!", renderHello(path));
    }
}
