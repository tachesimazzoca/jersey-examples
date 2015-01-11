package app.core.util;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import app.core.util.ImageUtils;

public class ImageUtilsTest {
    private File openTestFile(String path) {
        return new File(getClass().getResource("/test").getPath(), path);
    }

    private class TestPattern {
        public final Integer width;
        public final Integer height;
        public final String formatName;
        public final String source;
        public final String expected;

        public TestPattern(Integer width, Integer height, String formatName, String source,
                String expected) {
            this.width = width;
            this.height = height;
            this.formatName = formatName;
            this.source = source;
            this.expected = expected;
        }
    }

    private void runTestConvert(TestPattern[] patterns) throws IOException {
        for (int i = 0; i < patterns.length; i++) {
            FileInputStream is = new FileInputStream(openTestFile(patterns[i].source));
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageUtils.convert(is, os, patterns[i].formatName,
                    patterns[i].width, patterns[i].height);
            assertArrayEquals(FileUtils.readFileToByteArray(openTestFile(patterns[i].expected)),
                    os.toByteArray());
        }
    }

    @Test
    public void testConvert() throws IOException {
        TestPattern[] patterns = new TestPattern[] {
                new TestPattern(60, 60, "jpeg",
                        "/files/peacock.png", "/files/peacock_60x60.jpg"),
                new TestPattern(120, null, "jpeg",
                        "/files/peacock.png", "/files/peacock_120x.jpg"),
                new TestPattern(null, 60, "jpeg",
                        "/files/peacock.png", "/files/peacock_x60.jpg")
        };
        runTestConvert(patterns);
    }
}
