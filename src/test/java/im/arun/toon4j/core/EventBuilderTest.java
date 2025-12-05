package im.arun.toon4j.core;

import im.arun.toon4j.DecodeEvent;
import im.arun.toon4j.Normalize;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EventBuilderTest {

    @Test
    void buildsEventsForNestedObjectsAndArrays() {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", 1);
        user.put("tags", List.of("alpha", "beta"));

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("user", user);

        List<DecodeEvent> events = EventBuilder.buildEvents(Normalize.normalizeValue(root));

        assertEquals(12, events.size());
        assertInstanceOf(DecodeEvent.StartObject.class, events.get(0));
        assertEquals("user", ((DecodeEvent.KeyEvent) events.get(1)).key());
        assertInstanceOf(DecodeEvent.StartObject.class, events.get(2));
        assertEquals("id", ((DecodeEvent.KeyEvent) events.get(3)).key());
        assertEquals(1, ((DecodeEvent.PrimitiveEvent) events.get(4)).value());
        assertEquals("tags", ((DecodeEvent.KeyEvent) events.get(5)).key());
        assertEquals(2, ((DecodeEvent.StartArray) events.get(6)).length());
        assertEquals("alpha", ((DecodeEvent.PrimitiveEvent) events.get(7)).value());
        assertEquals("beta", ((DecodeEvent.PrimitiveEvent) events.get(8)).value());
        assertInstanceOf(DecodeEvent.EndArray.class, events.get(9));
        assertInstanceOf(DecodeEvent.EndObject.class, events.get(10));
        assertInstanceOf(DecodeEvent.EndObject.class, events.get(11));
    }
}
