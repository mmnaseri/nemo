package com.agileapes.nemo.contract.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is an extension to AbstractContext which provides a thread-safe data storage for the
 * underlying context to use.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 16:50)
 */
public abstract class AbstractThreadSafeContext<C> extends AbstractContext<C> {

    private final Map<String, C> map = new ConcurrentHashMap<String, C>();

    @Override
    protected Map<String, C> getMap() {
        return map;
    }

}
