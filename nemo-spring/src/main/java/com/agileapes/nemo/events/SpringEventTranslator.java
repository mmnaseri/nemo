package com.agileapes.nemo.events;

import com.agileapes.nemo.contract.Callback;
import com.agileapes.nemo.event.Event;
import com.agileapes.nemo.event.EventListener;
import com.agileapes.nemo.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/16/13, 3:58 PM)
 */
public class SpringEventTranslator implements EventListener<Event>, ApplicationContextAware {

    private static final Log log = LogFactory.getLog(SpringEventTranslator.class);
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(final Event event) {
        String canonicalName = getClass().getCanonicalName();
        canonicalName = canonicalName.substring(0, canonicalName.lastIndexOf(".") + 1) + event.getClass().getSimpleName();
        final Class eventClass;
        try {
            eventClass = ClassUtils.forName(canonicalName, applicationContext.getClassLoader());
        } catch (ClassNotFoundException e) {
            log.error("Unsupported Spring event: " + event.getClass().getSimpleName());
            return;
        }
        final Constructor constructor;
        try {
            //noinspection unchecked
            constructor = eventClass.getConstructor(SpringEventTranslator.class);
        } catch (Throwable e) {
            log.error("Unknown translation scheme for event: " + event.getClass().getSimpleName(), e);
            return;
        }
        final ApplicationEvent translated;
        try {
            translated = (ApplicationEvent) constructor.newInstance(this);
        } catch (Throwable e) {
            log.error("Failed to instantiated translation for " + event.getClass().getSimpleName(), e);
            return;
        }
        ReflectionUtils.withMethods(event.getClass())
                .filter(new GetterMethodFilter())
                .each(new Callback<Method>() {
                    @Override
                    public void perform(Method getter) {
                        final CollectionDSL.Wrapper<Method> setters = ReflectionUtils.withMethods(eventClass).filter(new SetterMethodFilter()).filter(new MethodPropertyFilter(ReflectionUtils.getPropertyName(getter.getName())));
                        if (setters.count() == 0) {
                            return;
                        }
                        final Method setter = setters.first();
                        try {
                            setter.invoke(translated, getter.invoke(event));
                        } catch (Throwable e) {
                            log.error("Failed to convert event data for field " + ReflectionUtils.getPropertyName(getter.getName()), e);
                        }
                    }
                });
        applicationContext.publishEvent(translated);
    }

}
