package com.agileapes.nemo.events.impl;

import com.agileapes.nemo.contract.Callback;
import com.agileapes.nemo.error.EventTranslationException;
import com.agileapes.nemo.error.WrappedError;
import com.agileapes.nemo.event.Event;
import com.agileapes.nemo.events.TranslationScheme;
import com.agileapes.nemo.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.agileapes.nemo.util.ReflectionUtils.withFields;
import static com.agileapes.nemo.util.ReflectionUtils.withMethods;

/**
 * This translation scheme will use reflection to translate all events registered with nemo (or any
 * other event under {@link #BUILT_IN_EVENT_PACKAGE}) into their equivalent, Spring-based evnets
 * locatable under {@link #BUILT_IN_TRANSLATED_EVENT_PACKAGE}.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/17/13, 11:53 AM)
 */
public class BuiltInTranslationScheme implements TranslationScheme {

    private static final Log log = LogFactory.getLog(BuiltInTranslationScheme.class);
    public static final String BUILT_IN_EVENT_PACKAGE = "com.agileapes.nemo.event.impl.events.";
    public static final String BUILT_IN_TRANSLATED_EVENT_PACKAGE = "com.agileapes.nemo.events.translated.";

    @Override
    public boolean accepts(Event event) {
        return event.getClass().getCanonicalName().startsWith(BUILT_IN_EVENT_PACKAGE);
    }

    @Override
    public ApplicationEvent translate(final Event event) throws EventTranslationException {
        String canonicalName = BUILT_IN_TRANSLATED_EVENT_PACKAGE + event.getClass().getSimpleName();
        final Class eventClass;
        try {
            eventClass = ClassUtils.forName(canonicalName, getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new EventTranslationException("Unsupported Spring event: " + event.getClass().getSimpleName());
        }
        final Constructor constructor;
        try {
            //noinspection unchecked
            constructor = eventClass.getConstructor(Object.class);
        } catch (Throwable e) {
            throw new EventTranslationException("Unknown translation scheme for event: " + event.getClass().getSimpleName(), e);
        }
        final ApplicationEvent translated;
        try {
            translated = (ApplicationEvent) constructor.newInstance(this);
        } catch (Throwable e) {
            throw new EventTranslationException("Failed to instantiate translation for " + event.getClass().getSimpleName(), e);
        }
        withMethods(event.getClass())
                .filter(new GetterMethodFilter())
                .each(new Callback<Method>() {
                    @Override
                    public void perform(Method getter) {
                        final CollectionDSL.Wrapper<Field> fields = withFields(eventClass).filter(new FieldNameFilter(ReflectionUtils.getPropertyName(getter.getName())));
                        if (fields.count() == 0) {
                            return;
                        }
                        final Field field = fields.first();
                        try {
                            field.setAccessible(true);
                            field.set(translated, getter.invoke(event));
                        } catch (Throwable e) {
                            log.error("Failed to convert event data for field " + ReflectionUtils.getPropertyName(getter.getName()), e);
                        }
                    }
                });
        return translated;
    }

    @Override
    public void translate(final ApplicationEvent event, final Event originalEvent) throws EventTranslationException {
        withMethods(originalEvent.getClass()).filter(new SetterMethodFilter())
                .each(new Callback<Method>() {
                    @Override
                    public void perform(Method item) {
                        try {
                            final Method getter = withMethods(event.getClass()).filter(new GetterMethodFilter()).filter(new MethodPropertyFilter(ReflectionUtils.getPropertyName(item.getName()))).first();
                            final Object value = getter.invoke(event);
                            item.invoke(originalEvent, value);
                        } catch (Throwable e) {
                            throw new WrappedError(e);
                        }
                    }
                });
    }

}
