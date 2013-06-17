package com.agileapes.nemo.event.impl;

import com.agileapes.nemo.event.Event;
import com.agileapes.nemo.event.EventListener;
import com.agileapes.nemo.util.ClassUtils;

/**
 * The SmartEventListener is basically a wrapper for any other EventListener, with the exception of being able
 * to determine whether the wrapped listener is listening for queried event type. This is made possible by taking
 * advantage of some reflection magic.
 *
 * @see ClassUtils#resolveTypeArgument(Class, Class)
 * @see #supportsEvent(com.agileapes.nemo.event.Event)
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/16/13, 3:16 PM)
 */
public class SmartEventListener<E extends Event> implements EventListener<E> {

    private final EventListener<E> listener;

    public SmartEventListener(EventListener<E> listener) {
        this.listener = listener;
    }

    /**
     * Will determine whether or not this listener (or the one being proxied through this event listener, rather)
     * is looking for an event of the given type.
     * @param event    the event against which the query will run.
     * @return {@code true} if it does
     */
    public boolean supportsEvent(Event event) {
        return ClassUtils.resolveTypeArgument(listener.getClass(), EventListener.class).isAssignableFrom(event.getClass());
    }

    @Override
    public void onApplicationEvent(E event) {
        listener.onApplicationEvent(event);
    }

}
