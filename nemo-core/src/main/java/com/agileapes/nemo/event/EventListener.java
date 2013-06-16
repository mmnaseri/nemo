package com.agileapes.nemo.event;

/**
 * The event listener interface enables implementing classes to identify a single event they listen to. Notice that
 * if an event listener listens to events of type {@code A}, it will also be notified of all event types that are
 * descendants of {@code A} as well.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/16/13, 3:02 PM)
 */
public interface EventListener<E extends Event> {

    /**
     * This method will be called whenever an event of the specified generic type occurs
     *
     * <strong>Note on events:</strong> some events will offer setters for certain properties. This means that
     * these events will be able to change these values for the calling object. Through that, event listeners can
     * manipulate the execution of the application.
     * @param event    the event object.
     */
    void onApplicationEvent(E event);

}
