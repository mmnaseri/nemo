package com.agileapes.nemo.events.impl;

import com.agileapes.nemo.contract.Callback;
import com.agileapes.nemo.contract.OrderedBean;
import com.agileapes.nemo.error.EventTranslationException;
import com.agileapes.nemo.event.Event;
import com.agileapes.nemo.events.TranslationScheme;
import com.agileapes.nemo.util.GetterMethodFilter;
import com.agileapes.nemo.util.ReflectionUtils;
import org.springframework.context.ApplicationEvent;

import java.lang.reflect.Method;

import static com.agileapes.nemo.util.ReflectionUtils.withMethods;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/17/13, 12:44 PM)
 */
public class GenericTranslationScheme implements TranslationScheme, OrderedBean {

    @Override
    public boolean accepts(Event event) {
        return true;
    }

    @Override
    public ApplicationEvent translate(final Event event) throws EventTranslationException {
        final GenericEvent genericEvent = new GenericEvent(this);
        withMethods(event.getClass()).filter(new GetterMethodFilter())
                .each(new Callback<Method>() {
                    @Override
                    public void perform(Method item) {
                        final Object value;
                        try {
                            value = item.invoke(event);
                        } catch (Throwable e) {
                            return;
                        }
                        genericEvent.setProperty(ReflectionUtils.getPropertyName(item.getName()), value);
                    }
                });
        return genericEvent;
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

}
