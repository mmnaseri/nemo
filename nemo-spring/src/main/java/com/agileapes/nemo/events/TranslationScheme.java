package com.agileapes.nemo.events;

import com.agileapes.nemo.error.EventTranslationException;
import com.agileapes.nemo.event.Event;
import org.springframework.context.ApplicationEvent;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/17/13, 11:51 AM)
 */
public interface TranslationScheme {

    boolean accepts(Event event);

    ApplicationEvent translate(Event event) throws EventTranslationException;

}
