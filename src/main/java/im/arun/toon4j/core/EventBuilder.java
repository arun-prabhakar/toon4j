package im.arun.toon4j.core;

import im.arun.toon4j.DecodeEvent;
import im.arun.toon4j.Normalize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds decode events from a decoded value tree.
 */
public final class EventBuilder {
    private EventBuilder() {
    }

    public static List<DecodeEvent> buildEvents(Object value) {
        List<DecodeEvent> events = new ArrayList<>();
        appendValue(events, value);
        return events;
    }

    @SuppressWarnings("unchecked")
    private static void appendValue(List<DecodeEvent> events, Object value) {
        if (Normalize.isJsonObject(value)) {
            events.add(new DecodeEvent.StartObject());
            Map<String, Object> map = (Map<String, Object>) value;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                events.add(new DecodeEvent.KeyEvent(entry.getKey()));
                appendValue(events, entry.getValue());
            }
            events.add(new DecodeEvent.EndObject());
            return;
        }

        if (Normalize.isJsonArray(value)) {
            List<Object> list = (List<Object>) value;
            events.add(new DecodeEvent.StartArray(list.size()));
            for (Object item : list) {
                appendValue(events, item);
            }
            events.add(new DecodeEvent.EndArray());
            return;
        }

        events.add(new DecodeEvent.PrimitiveEvent(value));
    }
}
