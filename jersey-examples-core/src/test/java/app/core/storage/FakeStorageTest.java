package app.core.storage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FakeStorageTest {
    @Test
    public void testCreateAndRead() {
        FakeStorage storage = new FakeStorage<String>(3);
        String foo = storage.create("foo");
        assertEquals("foo", storage.read(foo).orNull());
        assertEquals(1, storage.size());
        String bar = storage.create("bar");
        assertEquals("foo", storage.read(foo).orNull());
        assertEquals("bar", storage.read(bar).orNull());
        assertEquals(2, storage.size());
        String baz = storage.create("baz");
        assertEquals("foo", storage.read(foo).orNull());
        assertEquals("bar", storage.read(bar).orNull());
        assertEquals("baz", storage.read(baz).orNull());
        assertEquals(3, storage.size());

        // Create over maxEntries
        String qux = storage.create("qux");
        assertEquals("qux", storage.read(qux).orNull());
        assertEquals(3, storage.size());
    }
}
