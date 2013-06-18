package com.agileapes.nemo.events;

import com.agileapes.nemo.error.EventTranslationException;
import com.agileapes.nemo.event.Event;
import org.springframework.context.ApplicationEvent;

/**
 * Translation schemes are the abstraction of the process of translating nemo events into Spring events.
 * This is done so that nemo events can have subscribers through the {@link org.springframework.context.ApplicationListener}
 * interface as well as the nemo-based {@link com.agileapes.nemo.event.EventListener} interface.
 *
 * The translation schemes are provided to ensure that end-users of the framework do not need to worry about
 * how they should be listening to nemo's events as they are triggered.
 *
 * Translators are responsible for declaring which events
 * they are capable of translating and which events are beyond their scope.
 *
 * @see #translate(Event)
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/17/13, 11:51 AM)
 */
public interface TranslationScheme {

    /**
     * Will determine whether this scheme handles the translation of an event into an instance of ApplicationEvent
     * and back to its original form
     * @param event    the event being fired through nemo
     * @return {@code true} if it does
     */
    boolean accepts(Event event);

    /**
     * This method will be called whenever an event accepted by this translator is triggered.
     * Note that translators need not be exclusive, i.e, each event might have more than one candidate
     * translator. Should these translators be prioritized arbitrarily, they will be applied and used
     * in a non-deterministic manner. This should not be a problem so long as they do not produce multiple
     * types of events for the single input event, which might result in some instances of an event's
     * occurrence being lost to some listeners which can only listen to one of them at any given time.
     *
     * @param event    the event being fired.
     * @return the translated, Spring-based event.
     * @throws EventTranslationException
     */
    ApplicationEvent translate(Event event) throws EventTranslationException;

    /**
     * This method is called by the translator so that any properties altered by {@link org.springframework.context.ApplicationListener}
     * beans to the application event can be reflected back to its original event object.
     * @param event            the event fired through Spring's application context.
     * @param originalEvent    the original event fired through nemo
     * @throws EventTranslationException
     */
    void translate(ApplicationEvent event, Event originalEvent) throws EventTranslationException;

}
