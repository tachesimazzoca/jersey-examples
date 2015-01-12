package app.models;

import com.google.common.base.Optional;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TempFileHelper {
    private File directory;

    public TempFileHelper(File directory) {
        if (!directory.exists() || !directory.isDirectory())
            throw new IllegalArgumentException("The directory does not exist.");
        this.directory = directory;
    }

    public TempFileHelper(String directoryPath) {
        this(new File(directoryPath));
    }

    public File create(InputStream input, String prefix, String suffix)
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
