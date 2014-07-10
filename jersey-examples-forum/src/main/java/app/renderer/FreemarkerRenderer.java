package app.renderer;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Locale;

public class FreemarkerRenderer implements Renderer {
    private static class TemplateLoader extends
            CacheLoader<String, Configuration> {
        @Override
        public Configuration load(String key) throws Exception {
            final Configuration configuration = new Configuration();
            configuration.setObjectWrapper(new DefaultObjectWrapper());
            configuration.loadBuiltInEncodingMap();
            configuration.setDefaultEncoding(Charsets.UTF_8.name());
            configuration.setDirectoryForTemplateLoading(new File("/"));
            return configuration;
        }
    }

    private final LoadingCache<String, Configuration> configurationCache;

    public FreemarkerRenderer() {
        this.configurationCache = CacheBuilder.newBuilder()
                .concurrencyLevel(128)
                .build(new TemplateLoader());
    }

    @Override
    public void render(String path, Object attr, Locale locale,
            OutputStream output) throws IOException, TemplateException {
        final Configuration configuration =
                configurationCache.getUnchecked(path);
        final Charset charset = Charset.forName(
                configuration.getEncoding(locale));
        String realpath;
        if (path.startsWith("/")) {
            realpath = path;
        } else {
            realpath = this.getClass().getResource(
                    "/views/freemarker").getPath() + "/" + path;
        }
        final Template template = configuration.getTemplate(
                realpath, charset.name());
        template.process(attr,
                new OutputStreamWriter(output, template.getEncoding()));
    }
}
