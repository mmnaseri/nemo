package com.agileapes.nemo.contract;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 18:29)
 */
public interface Cache<K, V> {

    void write(K key, V value);

    V read(K key);

    boolean has(K key);

}