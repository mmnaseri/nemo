package com.agileapes.nemo.events;

import com.agileapes.nemo.contract.Filter;
import com.agileapes.nemo.contract.impl.AbstractThreadSafeContext;
import com.agileapes.nemo.error.AggregatedException;
import com.agileapes.nemo.error.EventTranslationException;
import com.agileapes.nemo.event.Event;
import com.agileapes.nemo.event.EventListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.List;

import static com.agileapes.nemo.util.CollectionDSL.with;

/**
 * This class will act as a repository for all {@link TranslationScheme} objects and will be run with top priority
 * to fire Spring-based events for each of the nemo-related events.
 *
 * All events will be translated, as the {@link com.agileapes.nemo.events.impl.GenericTranslationScheme} is always
 * registered with this translator.
 *
 * Candidates will be tried sequentially in case they fail, until an unfailing candidate is found which is capable
 * or producing a translated event successfully.
 *
 * Translators will be applied in ascending order as determined by {@link com.agileapes.nemo.contract.OrderedBean}.
 * This is important to note, since developers might mistakenly implement {@link Ordered} instead.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/16/13, 3:58 PM)
 */
public class SpringEventTranslator extends AbstractThreadSafeContext<TranslationScheme> implements EventListener<Event>, ApplicationContextAware, Ordered {

    private static final Log log = LogFactory.getLog(SpringEventTranslator.class);
    private ApplicationContext applicationContext;

    @Override
    protected Class<TranslationScheme> getType() {
        return TranslationScheme.class;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(final Event event) {
        final List<TranslationScheme> list = with(getOrderedBeans())
                .filter(new Filter<TranslationScheme>() {
                    @Override
                    public boolean accepts(TranslationScheme item) {
                        return item.accepts(event);
                    }
                }).list();
        if (list.isEmpty()) {
             log.error("No translation scheme found for event " + event.getClass().getCanonicalName());
            return;
        }
        log.info("Found " + list.size() + " ways to translate event");
        final List<Throwable> errors = new ArrayList<Throwable>();
        ApplicationEvent applicationEvent = null;
        TranslationScheme targetScheme = null;
        for (TranslationScheme scheme : list) {
            try {
                applicationEvent = scheme.translate(event);
                targetScheme = scheme;
                break;
            } catch (Throwable e) {
                errors.add(e);
            }
        }
        if (applicationEvent == null || targetScheme == null) {
            throw new FatalBeanException("Failed to translated event: " + event.getClass().getCanonicalName(), new AggregatedException(errors));
        }
        applicationContext.publishEvent(applicationEvent);
        try {
            targetScheme.translate(applicationEvent, event);
        } catch (EventTranslationException e) {
            throw new FatalBeanException("Could not translate the event back into its original form");
        }
    }

}
