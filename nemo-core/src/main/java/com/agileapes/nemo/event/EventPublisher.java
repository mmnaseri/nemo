package com.agileapes.nemo.event;

/**
 * The event multicaster
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/15/13, 8:55 PM)
 */
public interface EventPublisher {

    <E extends Event> E publishEvent(E event);

}
