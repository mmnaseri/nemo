package com.agileapes.nemo.contract.impl;

import com.agileapes.nemo.contract.ActionDiscoverer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Map;

/**
 * This is intended as a discovery scheme that will turn up all actions that are descendants of a certain
 * type
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/15/13, 5:01 PM)
 */
public class TypedActionDiscoverer extends ActionDiscoverer {

    private static final Log log = LogFactory.getLog(ActionDiscoverer.class.getCanonicalName().concat(".typed"));
    private final Class<?> type;

    public TypedActionDiscoverer(Class<?> type) {
        this.type = type;
    }

    @Override
    protected Map<String, Object> discover(ConfigurableListableBeanFactory factory) {
        log.info("Looking for actions extending " + type.getCanonicalName());
        //noinspection unchecked
        final Map<String, Object> map = (Map<String, Object>) factory.getBeansOfType(type, false, true);
        log.info("Discovered " + map.size() + " action(s)");
        return map;
    }

}
