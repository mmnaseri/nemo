package com.agileapes.nemo.event;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/15/13, 8:58 PM)
 */
public abstract class Event {

    private final Object source;

    public Event(Object source) {
        this.source = source;
    }

    public Object getSource() {
        return source;
    }

}
