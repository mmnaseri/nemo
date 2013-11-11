package com.agileapes.nemo.util;

import com.agileapes.couteau.basics.api.Processor;
import com.agileapes.couteau.reflection.util.ReflectionUtils;
import com.agileapes.couteau.reflection.util.assets.GetterMethodFilter;
import com.agileapes.nemo.error.WrappedError;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class will help convert an annotation an all its properties into maps.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/13/13, 12:24 PM)
 */
public class AnnotationPropertyBuilder {

    private final Map<String, Object> values = new HashMap<String, Object>();
    private final String name;

    public AnnotationPropertyBuilder(final Annotation annotation) {
        name = "@" + annotation.annotationType().getCanonicalName();
        ReflectionUtils.withMethods(annotation.annotationType())
                .keep(new GetterMethodFilter(true))
                .each(new Processor<Method>() {
                    @Override
                    public void process(Method item) {
                        Object value = null;
                        try {
                            value = item.invoke(annotation);
                        } catch (Exception e) {
                            throw new WrappedError(e);
                        }
                        if (value instanceof Annotation) {
                            value = new AnnotationPropertyBuilder((Annotation) value).getValues();
                        }
                        values.put(item.getName(), value);
                    }
                });
    }

    public Map<String, Object> getValues() {
        return Collections.unmodifiableMap(values);
    }

    public void addTo(Properties properties) {
        properties.put(name, getValues());
    }

}
