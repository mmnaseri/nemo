package com.agileapes.nemo.contract;

import com.agileapes.couteau.basics.api.Transformer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Collections;
import java.util.Map;

/**
 * The action discoverer will take in a bean factory, and based on a certain criteria discern which beans
 * should be put aside as actions to be registered with the executor context
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/15/13, 4:57 PM)
 */
public abstract class ActionDiscoverer implements Transformer<ConfigurableListableBeanFactory, Map<String, Object>> {

    @Override
    public Map<String, Object> map(ConfigurableListableBeanFactory factory) {
        final Map<String, Object> map = discover(factory);
        return map == null ? Collections.<String, Object>emptyMap() : Collections.unmodifiableMap(map);
    }

    /**
     * This method will be used to give the discoverer a chance to look seep through the bean
     * factory and determine which beans should be uncovered.
     * @param factory    the bean factory for the application
     * @return a map of action names to action objects
     */
    protected abstract Map<String, Object> discover(ConfigurableListableBeanFactory factory);

}
