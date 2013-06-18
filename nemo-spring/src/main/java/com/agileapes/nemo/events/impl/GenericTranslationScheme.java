package com.agileapes.nemo.events.impl;

import com.agileapes.nemo.contract.Callback;
import com.agileapes.nemo.contract.OrderedBean;
import com.agileapes.nemo.error.EventTranslationException;
import com.agileapes.nemo.error.WrappedError;
import com.agileapes.nemo.event.Event;
import com.agileapes.nemo.events.TranslationScheme;
import com.agileapes.nemo.util.GetterMethodFilter;
import com.agileapes.nemo.util.ReflectionUtils;
import com.agileapes.nemo.util.SetterMethodFilter;
import org.springframework.context.ApplicationEvent;

import java.lang.reflect.Method;

import static com.agileapes.nemo.util.ReflectionUtils.withMethods;

/**
 * This translation scheme will use reflection to invoke getter methods on any given event of any
 * type to create a mapped instance of {@link GenericEvent}.
 *
 * This is essentially a fallback translator applicable to any kind of event. This translator has least
 * precedence over other translators, meaning that, barring the existence of other translators with
 * the same order of precedence, this translator will be run only when all other candidates fail.
 *
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
    public void translate(final ApplicationEvent event, final Event originalEvent) throws EventTranslationException {
        if (!(event instanceof GenericEvent)) {
            throw new EventTranslationException("This translator is not capable of translating event: " + event.getClass().getCanonicalName());
        }
        final GenericEvent genericEvent = (GenericEvent) event;
        withMethods(originalEvent.getClass()).filter(new SetterMethodFilter())
                .each(new Callback<Method>() {
                    @Override
                    public void perform(Method item) {
                        try {
                            item.invoke(originalEvent, ((GenericEvent) event).getProperties().get(ReflectionUtils.getPropertyName(item.getName())));
                        } catch (Throwable e) {
                            throw new WrappedError(e);
                        }
                    }
                });
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

}
