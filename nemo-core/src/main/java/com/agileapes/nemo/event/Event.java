package com.agileapes.nemo.event;

/**
 * The event class is the base of each event publisher throughout this application. Events are the unit of work for the
 * pub-sub-like mode of event distribution.
 *
 * @see EventListener
 * @see EventPublisher
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/15/13, 8:58 PM)
 */
public abstract class Event {

    private final Object source;

    /**
     * This constructor instantiates the event, while marking the source of the event. This will help with
     * subscribers which might want to decide on their reaction based on the different sources the event might
     * bubble up from
     * @param source    the source object
     */
    public Event(Object source) {
        this.source = source;
    }

    /**
     * @return the source object for this event
     */
    public Object getSource() {
        return source;
    }

}
