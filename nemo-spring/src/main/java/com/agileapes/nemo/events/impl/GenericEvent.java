package com.agileapes.nemo.events.impl;

import com.agileapes.nemo.events.TranslatedEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/17/13, 11:48 AM)
 */
public class GenericEvent extends TranslatedEvent {

    private final Map<String, Object> properties = new HashMap<String, Object>();

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the component that published the event (never {@code null})
     */
    public GenericEvent(Object source) {
        super(source);
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public void setProperty(String property, Object value) {
        properties.put(property, value);
    }

}
