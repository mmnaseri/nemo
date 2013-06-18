package com.agileapes.nemo.exec;

import com.agileapes.nemo.contract.Callback;
import com.agileapes.nemo.contract.Mapper;
import com.agileapes.nemo.event.EventListener;
import com.agileapes.nemo.event.impl.events.ExecutionStartedEvent;
import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static com.agileapes.nemo.util.CollectionDSL.sorted;
import static com.agileapes.nemo.util.CollectionDSL.with;

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
 * via {@link com.agileapes.nemo.exec.ExecutorContext#addEventListener(com.agileapes.nemo.event.EventListener)}
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
        with(names).each(new Callback<String>() {
            @Override
            public void perform(String item) {
                factor.set(Math.max(item.length(), target.length()));
            }
        });
        final Map<Double, String> map = with(names).key(new Mapper<String, Double>() {
            @Override
            public Double map(String item) {
                return (double) StringUtils.getLevenshteinDistance(item, target) / factor.get();
            }
        });
        final Set<Double> rejected = new HashSet<Double>();
        for (Map.Entry<Double, String> entry : map.entrySet()) {
            if (entry.getKey() > 0.6) {
                rejected.add(entry.getKey());
            }
        }
        with(rejected).each(new Callback<Double>() {
            @Override
            public void perform(Double item) {
                map.remove(item);
            }
        });
        if (map.isEmpty()) {
            return null;
        }
        final Double distance = with(sorted(map.keySet())).first();
        return new AbstractMap.SimpleEntry<Double, String>(distance, map.get(distance));
    }

    @Override
    public void onApplicationEvent(ExecutionStartedEvent event) {
        final String[] arguments = event.getArguments();
        if (arguments.length == 0 || arguments[0] == null || arguments[0].isEmpty() || arguments[0].startsWith("-")) {
            return;
        }
        final String target = arguments[0];
        final Set<String> names = event.getExecutorContext().getActionRegistry().getActions().keySet();
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
