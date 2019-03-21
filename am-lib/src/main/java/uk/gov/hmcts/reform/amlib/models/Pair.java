package uk.gov.hmcts.reform.amlib.models;

import java.util.Map;

public final class Pair<K, V> implements Map.Entry<K, V> {
    private final K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @SuppressWarnings("PMD") // Complains about setValue having a return. It is implemented this way in Map.Entry
    @Override
    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }

    @Override
    public String toString() {
        return "{" + key + "=" + value + '}';
    }
}
