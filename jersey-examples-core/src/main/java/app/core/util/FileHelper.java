package app.core.util;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class FileHelper {
    private static final Map<String, String> DEFAULT_MIME_TYPES =
            ImmutableMap.of("", "application/octet-stream");

    private static final NamingStrategy DEFAULT_NAMING_STRATEGY = new NamingStrategy() {
        public String buildRelativePath(String name, String extension) {
            if (extension.isEmpty())
                return name;
            else
                return name + "." + extension;
        }
    };

    private final File directory;
    private final Map<String, String> mimeTypes;
    private final NamingStrategy namingStrategy;

    public FileHelper(File directory) {
        this.directory = directory;
        this.mimeTypes = DEFAULT_MIME_TYPES;
        this.namingStrategy = DEFAULT_NAMING_STRATEGY;
    }

    public FileHelper(File directory, Map<String, String> mimeTypes) {
        this.directory = directory;
        this.mimeTypes = mimeTypes;
        this.namingStrategy = DEFAULT_NAMING_STRATEGY;
    }

    public FileHelper(File directory, Map<String, String> mimeTypes,
                      NamingStrategy namingStrategy) {
        this.directory = directory;
        this.mimeTypes = mimeTypes;
        this.namingStrategy = namingStrategy;
    }

    public Optional<Result> find(String name) {
        for (Map.Entry<String, String> entry : mimeTypes.entrySet()) {
            final File f = new File(directory,
                    namingStrategy.buildRelativePath(name, entry.getKey()));
            if (f.exists() && f.isFile())
                return Optional.of(new Result(f, entry.getValue()));
        }
        return Optional.absent();
    }

    public Optional<Result> find(String name, String extension) {
        if (!mimeTypes.containsKey(extension))
            return Optional.absent();
        final String mimeType = mimeTypes.get(extension);
        final File f = new File(directory,
                namingStrategy.buildRelativePath(name, extension));
        if (f.exists() && f.isFile())
            return Optional.of(new Result(f, mimeType));
        else
            return Optional.absent();
    }

    public boolean save(InputStream input, String name) throws IOException {
        return save(input, name, "");
    }

    public boolean save(InputStream input, String name, String extension) throws IOException {
        if (!mimeTypes.containsKey(extension))
            return false;
        final File f = new File(directory,
                namingStrategy.buildRelativePath(name, extension));
        if (f.exists() && !f.isFile())
            return false;
        FileUtils.copyInputStreamToFile(input, f);
        return true;
    }

    public boolean delete(String name) {
        boolean success = true;
        for (Map.Entry<String, String> entry : mimeTypes.entrySet()) {
            if (!delete(name, entry.getKey()))
                success = false;
        }
        return success;
    }

    public boolean delete(String name, String extension) {
        final File f = new File(directory,
                namingStrategy.buildRelativePath(name, extension));
        if (!f.exists())
            return true;
        if (!f.isFile())
            return false;
        return f.delete();
    }

    public interface NamingStrategy {
        public String buildRelativePath(String name, String extension);
    }

    public class Result {
        private final File file;
        private final String mimeType;

        public Result(File file, String mimeType) {
            this.file = file;
            this.mimeType = mimeType;
        }

        public File getFile() {
            return file;
        }

        public String getMimeType() {
            return mimeType;
        }
    }
}
