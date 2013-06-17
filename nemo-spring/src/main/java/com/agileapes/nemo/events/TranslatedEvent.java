package com.agileapes.nemo.events;

import org.springframework.context.ApplicationEvent;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/16/13, 4:27 PM)
 */
public abstract class TranslatedEvent extends ApplicationEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the component that published the event (never {@code null})
     */
    public TranslatedEvent(Object source) {
        super(source);
    }

}
