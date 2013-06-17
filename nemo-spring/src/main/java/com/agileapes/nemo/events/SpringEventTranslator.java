package com.agileapes.nemo.events;

import com.agileapes.nemo.contract.Filter;
import com.agileapes.nemo.contract.impl.AbstractThreadSafeContext;
import com.agileapes.nemo.error.AggregatedException;
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
        for (TranslationScheme scheme : list) {
            try {
                applicationEvent = scheme.translate(event);
                break;
            } catch (Throwable e) {
                errors.add(e);
            }
        }
        if (applicationEvent == null) {
            throw new FatalBeanException("Failed to translated event: " + event.getClass().getCanonicalName(), new AggregatedException(errors));
        }
        applicationContext.publishEvent(applicationEvent);
    }

}
