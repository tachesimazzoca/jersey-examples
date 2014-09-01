package app.core;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import app.core.Config;

public class FinderTest {
    private File testDirectory;

    public FinderTest() {
        Config config = Config.load("conf/application");
        testDirectory = new File(config.get("path.tmp") + "/test");
    }

    @Test
    public void testDefaultFinder() throws IOException {
        FileUtils.deleteDirectory(testDirectory);
        FileUtils.forceMkdir(testDirectory);

        Finder finder = new Finder(testDirectory);
        Optional<Finder.Result> resultOpt;
        resultOpt = finder.find("deadbeef");
        assertFalse(resultOpt.isPresent());
        resultOpt = finder.find("deadbeef", "txt");
        assertFalse(resultOpt.isPresent());

        InputStream in = getClass().getResourceAsStream("/test/upload/jersey_logo.png");
        assertTrue(finder.save(in, "test"));
        resultOpt = finder.find("test", "png");
        assertFalse(resultOpt.isPresent());
        resultOpt = finder.find("test");
        assertTrue(resultOpt.isPresent());
        Finder.Result result = resultOpt.get();
        assertEquals(new File(testDirectory, "test"), result.getFile());
        assertEquals("application/octet-stream", result.getMimeType());

        assertTrue(finder.delete("test"));
        resultOpt = finder.find("test");
        assertFalse(resultOpt.isPresent());

        FileUtils.deleteDirectory(testDirectory);
    }

    @Test
    public void testImageFinder() throws IOException {
        FileUtils.deleteDirectory(testDirectory);
        FileUtils.forceMkdir(testDirectory);

        Map<String, String> mimeTypes = ImmutableMap.of("png", "image/png");
        Finder.NamingStrategy namingStrategy = new Finder.NamingStrategy() {
            public String buildRelativePath(String name, String extension) {
                return extension + "/" + name + "." + extension;
            }
        };
        Finder finder = new Finder(testDirectory, mimeTypes, namingStrategy);

        InputStream in = getClass().getResourceAsStream("/test/upload/jersey_logo.png");
        assertFalse("It should be false if the extension is upsupported.",
                finder.save(in, "test"));
        assertTrue("It should be true if the extension is supported.",
                finder.save(in, "test", "png"));

        Optional<Finder.Result> resultOpt;
        resultOpt = finder.find("test");
        assertTrue("It should find one of supported types.",
                resultOpt.isPresent());
        Finder.Result result = resultOpt.get();
        assertEquals("It should return a converted file name.",
                new File(testDirectory, "png/test.png"), result.getFile());
        assertEquals("It should be a corresponding mime type.",
                "image/png", result.getMimeType());

        FileUtils.deleteDirectory(testDirectory);
    }
}
