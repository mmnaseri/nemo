package com.agileapes.nemo.contract.impl;

import com.agileapes.nemo.contract.ActionDiscoverer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * This is a generically configurable action discoverer that will turn up all actions which have
 * been annotated with the given annotation
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/15/13, 5:09 PM)
 */
public class AnnotatedActionDiscoverer extends ActionDiscoverer {

    private static final Log log = LogFactory.getLog(ActionDiscoverer.class.getCanonicalName().concat(".annotated"));
    private final Class<? extends Annotation> annotation;

    public AnnotatedActionDiscoverer(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    @Override
    protected Map<String, Object> discover(ConfigurableListableBeanFactory factory) {
        log.info("Looking for beans annotated with @" + annotation.getSimpleName());
        final Map<String, Object> map = factory.getBeansWithAnnotation(annotation);
        log.info("Discovered " + map.size() + " action(s)");
        return map;
    }

}
