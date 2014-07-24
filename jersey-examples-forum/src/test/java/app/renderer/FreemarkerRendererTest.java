package app.renderer;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.google.common.collect.ImmutableMap;

public class FreemarkerRendererTest {
    private String getResourcePath() {
        return this.getClass().getResource("/views/freemarker").getPath();
    }

    @Test(expected = IOException.class)
    public void testNoTemplate() throws Exception {
        Renderer renderer = new FreemarkerRenderer(getResourcePath());
        OutputStream os = new ByteArrayOutputStream();
        renderer.render("notemplate.ftl", ImmutableMap.<String, Object> of(), os);
    }

    @Test
    public void testRelativeTemplatePath() throws Exception {
        Renderer renderer = new FreemarkerRenderer(getResourcePath());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        renderer.render("index.ftl", ImmutableMap.<String, Object> of("name", "World!"), os);
        assertEquals("Hello World!", new String(os.toByteArray(), "UTF-8"));
    }
}
