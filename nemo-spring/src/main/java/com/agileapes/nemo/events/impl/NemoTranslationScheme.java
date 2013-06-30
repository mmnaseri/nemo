package com.agileapes.nemo.events.impl;

import com.agileapes.couteau.context.contract.Event;
import com.agileapes.couteau.context.spring.event.impl.AbstractMappedEventsTranslationScheme;
import com.agileapes.couteau.context.util.ClassUtils;

/**
 * This translation scheme will use reflection to translate all events registered with nemo (or any
 * other event under {@link #BUILT_IN_EVENT_PACKAGE}) into their equivalent, Spring-based events
 * locatable under {@link #BUILT_IN_TRANSLATED_EVENT_PACKAGE}.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/17/13, 11:53 AM)
 */
public class NemoTranslationScheme extends AbstractMappedEventsTranslationScheme {

    public static final String BUILT_IN_EVENT_PACKAGE = "com.agileapes.nemo.event.impl.events.";
    public static final String BUILT_IN_TRANSLATED_EVENT_PACKAGE = "com.agileapes.nemo.events.translated.";

    @Override
    public boolean handles(Event event) {
        return event.getClass().getCanonicalName().startsWith(BUILT_IN_EVENT_PACKAGE);
    }

    @Override
    protected Class<?> mapEvent(Class<? extends Event> aClass, ClassLoader classLoader) throws ClassNotFoundException {
        return ClassUtils.forName(BUILT_IN_TRANSLATED_EVENT_PACKAGE.concat(aClass.getSimpleName()), classLoader);
    }

}
