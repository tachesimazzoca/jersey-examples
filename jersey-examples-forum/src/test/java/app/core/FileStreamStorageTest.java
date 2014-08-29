package app.core;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class FileStreamStorageTest {
    private final Config config;

    public FileStreamStorageTest() {
        this.config = Config.load("conf/application");
    }

    private File createTempDirectory(String path) throws IOException {
        File testTmpDir = new File(config.get("path.tmp") + "/test");
        File tmpDir = new File(testTmpDir, path);
        FileUtils.forceMkdir(tmpDir);
        return tmpDir;
    }

    private void removeTempDirectory() throws IOException {
        File testTmpDir = new File(config.get("path.tmp") + "/test");
        FileUtils.deleteDirectory(testTmpDir);
    }

    @Test
    public void testCreate() throws IOException {
        File tmpDir = createTempDirectory("tmp");

        FileStreamStorage storage = new FileStreamStorage(tmpDir);
        String data = "Created at " + new java.util.Date();
        ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes());
        String key = storage.create(in);
        assertEquals(data, IOUtils.toString(storage.read(key).get()));
        storage.delete(key);
        assertNull(storage.read(key).orNull());

        removeTempDirectory();
    }

    @Test
    public void testWriteAndRead() throws IOException {
        File tmpDir = createTempDirectory("tmp");

        FileStreamStorage storage = new FileStreamStorage(tmpDir);
        String data = "Created at " + new java.util.Date();
        ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes());
        String key = java.util.UUID.randomUUID().toString();
        storage.write(key, in);
        assertEquals(data, IOUtils.toString(storage.read(key).get()));
        storage.delete(key);
        assertNull(storage.read(key).orNull());

        removeTempDirectory();
    }
}
