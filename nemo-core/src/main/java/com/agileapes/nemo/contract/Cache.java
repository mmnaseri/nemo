package com.agileapes.nemo.contract;

/**
 * This interface exposes the basic functionalities expected from a simple cache manager.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 18:29)
 */
public interface Cache<K, V> {

    /**
     * This will write a new value into the cache, possibly invalidating the current object residing at the slot
     * specified by the given key
     * @param key      the key
     * @param value    the value
     */
    void write(K key, V value);

    /**
     * This method reads the item associated with the given key, procuring it if it was a cache miss. This means that
     * if the item has previously been stored in the cache, this method will simply retrieve it, and if not, the implementation
     * must provide a way for the item to be provided to the outside world.
     * This method implements the read-through mechanism, and is expected to update the cache if the value requested has not
     * been cached previously.
     * @param key    the key
     * @return the cached value
     */
    V read(K key);

    /**
     * This method will determine whether the cache contains any items associated with the given key
     * @param key    the key
     * @return {@code true} if the cache contains the item
     */
    boolean has(K key);

}
