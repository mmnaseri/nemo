package com.agileapes.nemo.event.impl;

import com.agileapes.nemo.event.Event;
import com.agileapes.nemo.event.EventListener;
import com.agileapes.nemo.util.ClassUtils;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/16/13, 3:16 PM)
 */
public class SmartEventListener<E extends Event> implements EventListener<E> {

    private final EventListener<E> listener;

    public SmartEventListener(EventListener<E> listener) {
        this.listener = listener;
    }

    public boolean supportsEvent(Event event) {
        return ClassUtils.resolveTypeArgument(listener.getClass(), EventListener.class).isAssignableFrom(event.getClass());
    }

    @Override
    public void onApplicationEvent(E event) {
        listener.onApplicationEvent(event);
    }

}
