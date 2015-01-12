package app.models;

import static org.junit.Assert.*;
import org.junit.Test;

public class FileHelperFactoryTest {
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidNamingNumericTree() {
        FileHelperFactory.NAMING_NUMERIC_TREE.buildRelativePath("1234a", "jpg");
    }

    @Test
    public void testNamingNumericTree() {
        String path = FileHelperFactory.NAMING_NUMERIC_TREE.buildRelativePath("12345", "png");
        assertEquals("1/2/3/4/12345.png", path);
    }
}
