package app.core;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

public class Finder {
    private static NamingStrategy DEFAULT_NAMING_STRATEGY = new NamingStrategy() {
        public File getFile(File directory, String name, String extension) {
            if (extension.isEmpty())
                return new File(directory, name);
            else
                return new File(directory, name + "." + extension);
        }
    };

    private final File directory;
    private final Map<String, String> mimeTypes;
    private final NamingStrategy namingStrategy;

    public Finder(File directory) {
        this.directory = directory;
        this.mimeTypes = ImmutableMap.of("", "application/octet-stream");
        this.namingStrategy = DEFAULT_NAMING_STRATEGY;
    }

    public Finder(File directory, Map<String, String> mimeTypes) {
        this.directory = directory;
        this.mimeTypes = mimeTypes;
        this.namingStrategy = DEFAULT_NAMING_STRATEGY;
    }

    public Finder(File directory, Map<String, String> mimeTypes, NamingStrategy namingStrategy) {
        this.directory = directory;
        this.mimeTypes = mimeTypes;
        this.namingStrategy = namingStrategy;
    }

    public Optional<Result> find(String name) {
        for (Map.Entry<String, String> entry : mimeTypes.entrySet()) {
            final File f = namingStrategy.getFile(directory, name, entry.getKey());
            if (f.exists() && f.isFile())
                return Optional.of(new Result(f, entry.getValue()));
        }
        return Optional.absent();
    }

    public Optional<Result> find(String name, String extension) {
        if (!mimeTypes.containsKey(extension))
            return Optional.absent();
        final String mimeType = mimeTypes.get(extension);
        final File f = namingStrategy.getFile(directory, name, extension);
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
        final File f = namingStrategy.getFile(directory, name, extension);
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
        final File f = namingStrategy.getFile(directory, name, extension);
        if (!f.exists())
            return true;
        if (!f.isFile())
            return false;
        return f.delete();
    }

    public interface NamingStrategy {
        public File getFile(File directory, String name, String extension);
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
