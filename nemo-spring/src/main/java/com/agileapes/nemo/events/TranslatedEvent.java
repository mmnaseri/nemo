package com.agileapes.nemo.events;

import org.springframework.context.ApplicationEvent;

/**
 * This is the root to any translated event as done by built-in translation schemes. Developers are encouraged
 * to also extend this event when translating an event from nemo, so that the abstraction and the translation
 * process becomes traceable.
 *
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
