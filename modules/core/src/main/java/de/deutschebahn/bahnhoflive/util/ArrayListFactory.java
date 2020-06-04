package de.deutschebahn.bahnhoflive.util;

import java.util.ArrayList;

public class ArrayListFactory<K, V> implements MapContentPreserver.Factory<K, ArrayList<V>> {
    @Override
    public ArrayList<V> create(K key) {
        return new ArrayList<>();
    }
}
