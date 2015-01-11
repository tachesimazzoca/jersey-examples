package app.core.util;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.*;

public class FileHelperTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testDefaultFileHelper() throws IOException {
        File testDirectory = tempFolder.newFolder();

        FileHelper helper = new FileHelper(testDirectory);
        Optional<FileHelper.Result> resultOpt;
        resultOpt = helper.find("deadbeef");
        assertFalse(resultOpt.isPresent());
        resultOpt = helper.find("deadbeef", "txt");
        assertFalse(resultOpt.isPresent());

        InputStream in = getClass().getResourceAsStream("/test/files/jersey_logo.png");
        assertTrue(helper.save(in, "test"));
        resultOpt = helper.find("test", "png");
        assertFalse(resultOpt.isPresent());
        resultOpt = helper.find("test");
        assertTrue(resultOpt.isPresent());
        FileHelper.Result result = resultOpt.get();
        assertEquals(new File(testDirectory, "test"), result.getFile());
        assertEquals("application/octet-stream", result.getMimeType());

        assertTrue(helper.delete("test"));
        resultOpt = helper.find("test");
        assertFalse(resultOpt.isPresent());
    }

    @Test
    public void testImageFileHelper() throws IOException {
        File testDirectory = tempFolder.newFolder();

        Map<String, String> mimeTypes = ImmutableMap.of("png", "image/png");
        FileHelper.NamingStrategy namingStrategy = new FileHelper.NamingStrategy() {
            public String buildRelativePath(String name, String extension) {
                return extension + "/" + name + "." + extension;
            }
        };
        FileHelper helper = new FileHelper(testDirectory, mimeTypes, namingStrategy);

        InputStream in = getClass().getResourceAsStream("/test/files/jersey_logo.png");
        assertFalse("It should be false if the extension is not supported.",
                helper.save(in, "test"));
        assertTrue("It should be true if the extension is supported.",
                helper.save(in, "test", "png"));

        Optional<FileHelper.Result> resultOpt;
        resultOpt = helper.find("test");
        assertTrue("It should find one of supported types.",
                resultOpt.isPresent());
        FileHelper.Result result = resultOpt.get();
        assertEquals("It should return a converted file name.",
                new File(testDirectory, "png/test.png"), result.getFile());
        assertEquals("It should be a corresponding mime type.",
                "image/png", result.getMimeType());
    }
}
