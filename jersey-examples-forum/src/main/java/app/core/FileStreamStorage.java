package app.core;

import com.google.common.base.Optional;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FileStreamStorage implements Storage<InputStream> {
    private static final String DEFAULT_PREFIX = "tmp-";
    private static final String DEFAULT_SUFFIX = "";
    private final File directory;
    private final String prefix;
    private final String suffix;

    public FileStreamStorage(File directory) {
        this(directory, DEFAULT_PREFIX, DEFAULT_SUFFIX);
    }

    public FileStreamStorage(File directory, String prefix) {
        this(directory, prefix, DEFAULT_SUFFIX);
    }

    public FileStreamStorage(File directory, String prefix, String suffix) {
        if (directory == null || !directory.isDirectory())
            throw new IllegalArgumentException("The parameter directory must be a directory.");
        this.directory = directory;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String create(InputStream in) {
        String key = null;
        try {
            File f = File.createTempFile(prefix, suffix, directory);
            FileUtils.copyInputStreamToFile(in, f);
            key = f.getName();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return key;
    }

    public Optional<InputStream> read(String key) {
        File f = new File(directory, key);
        if (!f.isFile() || !f.canRead())
            return Optional.absent();
        InputStream in;
        try {
            in = FileUtils.openInputStream(f);
        } catch (IOException e) {
            in = null;
        }
        return Optional.fromNullable(in);
    }

    public void write(String key, InputStream in) {
        File f = new File(directory, key);
        try {
            FileUtils.copyInputStreamToFile(in, f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String key) {
        File f = new File(directory, key);
        FileUtils.deleteQuietly(f);
    }

    public Optional<File> getFile(String key) {
        return Optional.fromNullable(new File(directory, key));
    }
}
