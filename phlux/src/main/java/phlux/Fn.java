package phlux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A set of utility methods to create modified
 * collections instead of mutating them directly.
 */
class Fn {

    static <K, V> Map<K, V> with(Map<K, V> src, K key, V value) {
        HashMap<K, V> dst = new HashMap<>(src);
        dst.put(key, value);
        return Collections.unmodifiableMap(dst);
    }

    static <K, V> Map<K, V> without(Map<K, V> src, K key) {
        HashMap<K, V> dst = new HashMap<>(src);
        dst.remove(key);
        return Collections.unmodifiableMap(dst);
    }

    static <T> List<T> with(List<T> src, T value) {
        ArrayList<T> dst = new ArrayList<>(src);
        dst.add(value);
        return Collections.unmodifiableList(dst);
    }

    static <T> List<T> without(List<T> src, T value) {
        ArrayList<T> dst = new ArrayList<>(src);
        dst.remove(value);
        return Collections.unmodifiableList(dst);
    }
}
