package im.arun.toon4j;

/**
 * Streaming decode events for TOON data.
 */
public sealed interface DecodeEvent
    permits DecodeEvent.StartObject, DecodeEvent.EndObject, DecodeEvent.StartArray, DecodeEvent.EndArray, DecodeEvent.KeyEvent, DecodeEvent.PrimitiveEvent {

    record StartObject() implements DecodeEvent {}
    record EndObject() implements DecodeEvent {}
    record StartArray(int length) implements DecodeEvent {}
    record EndArray() implements DecodeEvent {}
    record KeyEvent(String key) implements DecodeEvent {}
    record PrimitiveEvent(Object value) implements DecodeEvent {}
}
