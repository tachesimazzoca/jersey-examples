package app.core.view;

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
import java.util.Map;

public class FreemarkerRenderer implements Renderer {
    private static final String TEMPLATE_EXT = ".ftl";

    private static class TemplateLoader extends
            CacheLoader<String, Configuration> {
        private final String directory;
        private final Map<String, Object> sharedVariables;

        public TemplateLoader(String directory,
                              Map<String, Object> sharedVariables) {
            this.directory = directory;
            this.sharedVariables = sharedVariables;
        }

        @Override
        public Configuration load(String key) throws Exception {
            final Configuration configuration = new Configuration();
            configuration.setObjectWrapper(new DefaultObjectWrapper());
            configuration.loadBuiltInEncodingMap();
            configuration.setDefaultEncoding(Charsets.UTF_8.name());
            configuration.setDirectoryForTemplateLoading(new File(directory));
            if (null != sharedVariables) {
                for (Map.Entry<String, Object> entry : sharedVariables.entrySet()) {
                    configuration.setSharedVariable(entry.getKey(), entry.getValue());
                }
            }
            return configuration;
        }
    }

    private final LoadingCache<String, Configuration> configurationCache;

    public FreemarkerRenderer(String dir) {
        this(dir, null);
    }

    public FreemarkerRenderer(String dir, Map<String, Object> sharedVariables) {
        this.configurationCache = CacheBuilder.newBuilder()
                .concurrencyLevel(128)
                .build(new TemplateLoader(dir, sharedVariables));
    }

    @Override
    public void render(String path, Object attr, OutputStream output) throws
            IOException, TemplateException {
        String realPath = path;
        if (!realPath.endsWith(TEMPLATE_EXT)) {
            realPath += TEMPLATE_EXT;
        }
        final Configuration configuration = configurationCache.getUnchecked(realPath);
        final Template template = configuration.getTemplate(realPath, Charsets.UTF_8.name());
        template.process(attr, new OutputStreamWriter(output, template.getEncoding()));
    }
}
