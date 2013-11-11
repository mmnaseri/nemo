package com.agileapes.nemo.assets;

import com.agileapes.couteau.basics.api.Filter;
import com.agileapes.couteau.basics.api.Processor;
import com.agileapes.couteau.basics.api.Transformer;
import com.agileapes.couteau.context.contract.EventListener;
import com.agileapes.nemo.event.impl.events.ExecutionStartedEvent;
import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.agileapes.couteau.basics.collections.CollectionWrapper.with;

/**
 * This class is an event listener which is capable of determining whether or not a certain
 * action name is a typographical error.
 *
 * This event will then try to intercept the error, correct it, and if it falls outside the
 * threshold of accepted closeness between the intended and the actual action names, suggest
 * an action names that is close to the one given by the user.
 *
 * This listener is not included by default in the context, and in case the developer wishes
 * to provide such a facility to its users, it can be added to the context as an event listener
 * via {@link com.agileapes.nemo.exec.ExecutorContext#addEventListener(com.agileapes.couteau.context.contract.EventListener)}
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/17/13, 12:53 PM)
 */
public class TypoCorrectionAsset implements EventListener<ExecutionStartedEvent> {

    private final double threshold;

    public TypoCorrectionAsset(double threshold) {
        this.threshold = threshold;
    }

    private static Map.Entry<Double, String> getClosestTarget(final String target, Set<String> names) {
        final AtomicLong factor = new AtomicLong(0);
        try {
            with(names).each(new Processor<String>() {
                @Override
                public void process(String item) {
                    factor.set(Math.max(item.length(), target.length()));
                }
            });
        } catch (Exception ignored) {
            return null;
        }
        List<Map.Entry<Double,String>> list;
        try {
            list = with(names).transform(new Transformer<String, Map.Entry<Double, String>>() {
                @Override
                public Map.Entry<Double, String> map(String item) {
                    return new AbstractMap.SimpleEntry<Double, String>((double) StringUtils.getLevenshteinDistance(item, target) / factor.get(), item);
                }
            }).list();
        } catch (Exception e) {
            return null;
        }
        final Set<Double> rejected = new HashSet<Double>();
        for (Map.Entry<Double, String> entry : list) {
            if (entry.getKey() > 0.6) {
                rejected.add(entry.getKey());
            }
        }
        try {
            //noinspection unchecked
            list = with(list).keep(new Filter<Map.Entry<Double, String>>() {
                @Override
                public boolean accepts(Map.Entry<Double, String> doubleStringEntry) {
                    return !rejected.contains(doubleStringEntry.getKey());
                }
            }).list();
        } catch (Exception e) {
            return null;
        }
        if (list.isEmpty()) {
            return null;
        }
        final Double distance;
        try {
            distance = with(list).transform(new Transformer<Map.Entry<Double, String>, Double>() {
                @Override
                public Double map(Map.Entry<Double, String> doubleStringEntry) {
                    return doubleStringEntry.getKey();
                }
            }).sort().first();
        } catch (Exception e) {
            return null;
        }
        final String entry;
        try {
            //noinspection unchecked
            entry = with(list).keep(new Filter<Map.Entry<Double, String>>() {
                @Override
                public boolean accepts(Map.Entry<Double, String> doubleStringEntry) {
                    return doubleStringEntry.getKey().equals(distance);
                }
            }).first().getValue();
        } catch (Exception e) {
            return null;
        }
        return new AbstractMap.SimpleEntry<Double, String>(distance, entry);
    }

    @Override
    public void onEvent(ExecutionStartedEvent event) {
        final String[] arguments = event.getArguments();
        if (arguments.length == 0 || arguments[0] == null || arguments[0].isEmpty() || arguments[0].startsWith("-")) {
            return;
        }
        final String target = arguments[0];
        final Set<String> names = event.getExecutorContext().getActionContext().getActions().keySet();
        if (names.contains(target)) {
            return;
        }
        final Map.Entry<Double, String> closestTarget = getClosestTarget(target, names);
        if (closestTarget == null) {
            return;
        }
        if (closestTarget.getKey() < threshold) {
            final PrintStream output = event.getExecutorContext().getOutput();
            output.println("No such target '" + target + "', we will assume you meant: " + closestTarget.getValue());
            output.println();
            arguments[0] = closestTarget.getValue();
            return;
        }
        throw new RuntimeException("No such target '" + target + "', did you mean '" + closestTarget.getValue() + "'?");
    }

}
