package app.core;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Optional;

public class Uploader {
    private File directory;

    public Uploader(File directory) {
        if (!directory.exists() || !directory.isDirectory())
            throw new IllegalArgumentException("The directory does not exist.");
        this.directory = directory;
    }

    public Uploader(String directoryPath) {
        this(new File(directoryPath));
    }

    public File upload(InputStream input, String prefix, String suffix)
            throws IOException {
        File tempfile = File.createTempFile(prefix, suffix, directory);
        FileUtils.copyInputStreamToFile(input, tempfile);
        return tempfile;
    }

    public Optional<File> read(String name) {
        File f = new File(directory, name);
        if (!f.exists() || !f.isFile())
            return Optional.absent();
        else
            return Optional.of(f);
    }
}
