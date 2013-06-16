package com.agileapes.nemo.event.impl;

import com.agileapes.nemo.contract.Callback;
import com.agileapes.nemo.contract.Filter;
import com.agileapes.nemo.contract.impl.AbstractThreadSafeContext;
import com.agileapes.nemo.event.Event;
import com.agileapes.nemo.event.EventListener;
import com.agileapes.nemo.event.EventPublisher;
import com.agileapes.nemo.util.CollectionDSL;

import java.util.List;

import static com.agileapes.nemo.util.CollectionDSL.with;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/16/13, 3:03 PM)
 */
public class DefaultEventPublisher extends AbstractThreadSafeContext<EventListener> implements EventPublisher {

    @Override
    protected Class<EventListener> getType() {
        return EventListener.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Event> E publishEvent(final E event) {
        final List<EventListener> orderedBeans = getOrderedBeans();
        final CollectionDSL.Wrapper<EventListener> with = with(orderedBeans);
        final CollectionDSL.Wrapper<EventListener> filter = with.filter(new Filter<EventListener>() {
            @Override
            public boolean accepts(EventListener item) {
                final SmartEventListener listener;
                if (item instanceof SmartEventListener) {
                    listener = (SmartEventListener) item;
                } else {
                    listener = new SmartEventListener(item);
                }
                return listener.supportsEvent(event);
            }
        });
        final CollectionDSL.Wrapper<EventListener> each = filter.each(new Callback<EventListener>() {
            @Override
            public void perform(EventListener item) {
                item.onApplicationEvent(event);
            }
        });
        return event;
    }

}
