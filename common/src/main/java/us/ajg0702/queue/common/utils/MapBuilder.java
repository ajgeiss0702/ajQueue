package us.ajg0702.queue.common.utils;

import java.util.LinkedHashMap;

public class MapBuilder<K, V> extends LinkedHashMap<K, V> {
    @SuppressWarnings("unchecked")
    public MapBuilder(Object... entries) {
        for (int i = 0; i < entries.length; i += 2) {
            put((K) entries[i], (V) entries[i+1]);
        }
    }
}
