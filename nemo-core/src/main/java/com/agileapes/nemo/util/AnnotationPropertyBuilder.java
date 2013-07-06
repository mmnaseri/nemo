package com.agileapes.nemo.util;

import com.agileapes.couteau.basics.api.Filter;
import com.agileapes.couteau.basics.api.Processor;
import com.agileapes.couteau.reflection.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
        try {
            ReflectionUtils.withMethods(annotation.annotationType())
                    .keep(new Filter<Method>() {
                        @Override
                        public boolean accepts(Method item) {
                            return Modifier.isPublic(item.getModifiers()) && !Modifier.isStatic(item.getModifiers())
                                    && item.getParameterTypes().length == 0 && !item.getReturnType().equals(void.class);
                        }
                    })
                    .each(new Processor<Method>() {
                        @Override
                        public void process(Method item) throws Exception {
                            Object value = item.invoke(annotation);
                            if (value instanceof Annotation) {
                                value = new AnnotationPropertyBuilder((Annotation) value).getValues();
                            }
                            values.put(item.getName(), value);
                        }
                    });
        } catch (Exception ignored) {
        }
    }

    public Map<String, Object> getValues() {
        return Collections.unmodifiableMap(values);
    }

    public void addTo(Properties properties) {
        properties.put(name, getValues());
    }

}
