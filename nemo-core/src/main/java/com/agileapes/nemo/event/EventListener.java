package com.agileapes.nemo.event;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/16/13, 3:02 PM)
 */
public interface EventListener<E extends Event> {

    void onApplicationEvent(E event);

}
