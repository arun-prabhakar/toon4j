package im.arun.toon4j.core;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for ObjectPool class.
 * Tests thread-local pooling, object reuse, and memory management.
 */
class ObjectPoolTest {

    @Test
    void testGetStringBuilderNotNull() {
        StringBuilder sb = ObjectPool.getStringBuilder();
        assertNotNull(sb);
    }

    @Test
    void testGetStringBuilderIsEmpty() {
        StringBuilder sb = ObjectPool.getStringBuilder();
        assertEquals(0, sb.length(), "StringBuilder should be cleared");
    }

    @Test
    void testGetStringBuilderReuse() {
        StringBuilder sb1 = ObjectPool.getStringBuilder();
        sb1.append("test content");
        ObjectPool.returnStringBuilder(sb1);

        StringBuilder sb2 = ObjectPool.getStringBuilder();
        assertEquals(0, sb2.length(), "Reused StringBuilder should be cleared");
    }

    @Test
    void testGetStringBuilderSameInstance() {
        StringBuilder sb1 = ObjectPool.getStringBuilder();
        ObjectPool.returnStringBuilder(sb1);

        StringBuilder sb2 = ObjectPool.getStringBuilder();
        assertSame(sb1, sb2, "Same thread should get same StringBuilder instance");
    }

    @Test
    void testReturnLargeStringBuilder() {
        StringBuilder sb = ObjectPool.getStringBuilder();

        // Make it large (> 4096)
        for (int i = 0; i < 5000; i++) {
            sb.append('x');
        }
        ObjectPool.returnStringBuilder(sb);

        StringBuilder sb2 = ObjectPool.getStringBuilder();
        assertNotSame(sb, sb2, "Large StringBuilder should be discarded");
        assertTrue(sb2.capacity() < sb.capacity(), "New StringBuilder should have smaller capacity");
    }

    @Test
    void testGetArrayListNotNull() {
        List<Object> list = ObjectPool.getArrayList();
        assertNotNull(list);
    }

    @Test
    void testGetArrayListIsEmpty() {
        List<Object> list = ObjectPool.getArrayList();
        assertTrue(list.isEmpty(), "ArrayList should be cleared");
    }

    @Test
    void testGetArrayListReuse() {
        List<Object> list1 = ObjectPool.getArrayList();
        list1.add("item1");
        list1.add("item2");
        ObjectPool.returnArrayList(list1);

        List<Object> list2 = ObjectPool.getArrayList();
        assertTrue(list2.isEmpty(), "Reused ArrayList should be cleared");
    }

    @Test
    void testGetArrayListSameInstance() {
        List<Object> list1 = ObjectPool.getArrayList();
        ObjectPool.returnArrayList(list1);

        List<Object> list2 = ObjectPool.getArrayList();
        assertSame(list1, list2, "Same thread should get same ArrayList instance");
    }

    @Test
    void testReturnLargeArrayList() {
        List<Object> list = ObjectPool.getArrayList();

        // Make it large (> 256)
        for (int i = 0; i < 300; i++) {
            list.add(i);
        }
        ObjectPool.returnArrayList(list);

        List<Object> list2 = ObjectPool.getArrayList();
        assertNotSame(list, list2, "Large ArrayList should be discarded");
        assertTrue(list2.size() == 0, "New ArrayList should be empty");
    }

    @Test
    void testMultipleGetStringBuilder() {
        StringBuilder sb1 = ObjectPool.getStringBuilder();
        sb1.append("test1");

        StringBuilder sb2 = ObjectPool.getStringBuilder();
        sb2.append("test2");

        // Second call should return same instance (cleared)
        assertSame(sb1, sb2);
        assertEquals("test2", sb2.toString());
    }

    @Test
    void testMultipleGetArrayList() {
        List<Object> list1 = ObjectPool.getArrayList();
        list1.add("item1");

        List<Object> list2 = ObjectPool.getArrayList();
        list2.add("item2");

        // Second call should return same instance (cleared)
        assertSame(list1, list2);
        assertEquals(1, list2.size());
        assertEquals("item2", list2.get(0));
    }

    @Test
    void testStringBuilderUsagePattern() {
        StringBuilder sb = ObjectPool.getStringBuilder();
        sb.append("Hello");
        sb.append(" ");
        sb.append("World");
        String result = sb.toString();

        assertEquals("Hello World", result);

        ObjectPool.returnStringBuilder(sb);

        StringBuilder sb2 = ObjectPool.getStringBuilder();
        assertEquals(0, sb2.length());
    }

    @Test
    void testArrayListUsagePattern() {
        List<Object> list = ObjectPool.getArrayList();
        list.add(1);
        list.add(2);
        list.add(3);

        assertEquals(3, list.size());

        ObjectPool.returnArrayList(list);

        List<Object> list2 = ObjectPool.getArrayList();
        assertTrue(list2.isEmpty());
    }

    @Test
    void testReturnStringBuilderSmallSize() {
        StringBuilder sb = ObjectPool.getStringBuilder();
        sb.append("small");
        ObjectPool.returnStringBuilder(sb);

        StringBuilder sb2 = ObjectPool.getStringBuilder();
        assertSame(sb, sb2, "Small StringBuilder should be reused");
    }

    @Test
    void testReturnArrayListSmallSize() {
        List<Object> list = ObjectPool.getArrayList();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }
        ObjectPool.returnArrayList(list);

        List<Object> list2 = ObjectPool.getArrayList();
        assertSame(list, list2, "Small ArrayList should be reused");
    }

    @Test
    void testReturnStringBuilderBoundary() {
        StringBuilder sb = ObjectPool.getStringBuilder();

        // Exactly 4096 characters
        for (int i = 0; i < 4096; i++) {
            sb.append('x');
        }
        ObjectPool.returnStringBuilder(sb);

        StringBuilder sb2 = ObjectPool.getStringBuilder();
        assertSame(sb, sb2, "StringBuilder at boundary (4096) should be reused");
    }

    @Test
    void testReturnStringBuilderJustOverBoundary() {
        StringBuilder sb = ObjectPool.getStringBuilder();

        // 4097 characters (over boundary)
        for (int i = 0; i < 4097; i++) {
            sb.append('x');
        }
        ObjectPool.returnStringBuilder(sb);

        StringBuilder sb2 = ObjectPool.getStringBuilder();
        assertNotSame(sb, sb2, "StringBuilder over boundary should be discarded");
    }

    @Test
    void testReturnArrayListBoundary() {
        List<Object> list = ObjectPool.getArrayList();

        // Exactly 256 items
        for (int i = 0; i < 256; i++) {
            list.add(i);
        }
        ObjectPool.returnArrayList(list);

        List<Object> list2 = ObjectPool.getArrayList();
        assertSame(list, list2, "ArrayList at boundary (256) should be reused");
    }

    @Test
    void testReturnArrayListJustOverBoundary() {
        List<Object> list = ObjectPool.getArrayList();

        // 257 items (over boundary)
        for (int i = 0; i < 257; i++) {
            list.add(i);
        }
        ObjectPool.returnArrayList(list);

        List<Object> list2 = ObjectPool.getArrayList();
        assertNotSame(list, list2, "ArrayList over boundary should be discarded");
    }

    @Test
    void testThreadIsolation() throws InterruptedException {
        StringBuilder mainThreadSB = ObjectPool.getStringBuilder();
        mainThreadSB.append("main");

        final StringBuilder[] otherThreadSB = new StringBuilder[1];

        Thread thread = new Thread(() -> {
            StringBuilder sb = ObjectPool.getStringBuilder();
            sb.append("other");
            otherThreadSB[0] = sb;
        });

        thread.start();
        thread.join();

        assertNotSame(mainThreadSB, otherThreadSB[0],
            "Different threads should have different StringBuilder instances");
    }

    @Test
    void testThreadIsolationArrayList() throws InterruptedException {
        List<Object> mainThreadList = ObjectPool.getArrayList();
        mainThreadList.add("main");

        final List<Object>[] otherThreadList = new List[1];

        Thread thread = new Thread(() -> {
            List<Object> list = ObjectPool.getArrayList();
            list.add("other");
            otherThreadList[0] = list;
        });

        thread.start();
        thread.join();

        assertNotSame(mainThreadList, otherThreadList[0],
            "Different threads should have different ArrayList instances");
    }

    @Test
    void testStringBuilderInitialCapacity() {
        StringBuilder sb = ObjectPool.getStringBuilder();
        // Default initial capacity should be 512
        assertTrue(sb.capacity() >= 512);
    }

    @Test
    void testArrayListType() {
        List<Object> list = ObjectPool.getArrayList();
        assertTrue(list instanceof ArrayList,
            "Pooled list should be an ArrayList");
    }

    @Test
    void testReturnStringBuilderNull() {
        // Should not throw exception
        assertDoesNotThrow(() -> ObjectPool.returnStringBuilder(null));
    }

    @Test
    void testReturnArrayListNull() {
        // Should not throw exception
        assertDoesNotThrow(() -> ObjectPool.returnArrayList(null));
    }

    @Test
    void testConcurrentUsageInSameThread() {
        StringBuilder sb1 = ObjectPool.getStringBuilder();
        sb1.append("first");

        // Without returning, get again
        StringBuilder sb2 = ObjectPool.getStringBuilder();

        // Should be same instance, now cleared
        assertSame(sb1, sb2);
        assertEquals(0, sb2.length());
    }

    @Test
    void testMultipleReturns() {
        StringBuilder sb = ObjectPool.getStringBuilder();
        ObjectPool.returnStringBuilder(sb);
        ObjectPool.returnStringBuilder(sb);
        ObjectPool.returnStringBuilder(sb);

        // Should not cause issues
        StringBuilder sb2 = ObjectPool.getStringBuilder();
        assertNotNull(sb2);
    }
}
