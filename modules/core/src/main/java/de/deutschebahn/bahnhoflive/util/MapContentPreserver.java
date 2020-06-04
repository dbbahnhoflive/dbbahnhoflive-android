package de.deutschebahn.bahnhoflive.util;

import java.util.Map;

public class MapContentPreserver<K, V> {

    public interface Factory<K, V> {
        V create(K key);
    }

    private final Map<K, V> map;

    private final Factory<K, ? extends V> factory;

    public MapContentPreserver(Map<K, V> map, Factory<K, ? extends V> factory) {
        this.map = map;
        this.factory = factory;
    }

    public V get(K key) {
        V value = map.get(key);

        if (value == null) {
            value = factory.create(key);
            map.put(key, value);
        }

        return value;
    }

    public Map<K, V> getMap() {
        return map;
    }
}
