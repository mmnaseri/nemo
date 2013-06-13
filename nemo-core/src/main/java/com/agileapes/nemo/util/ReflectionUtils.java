/*
 * Copyright (c) 2013. AgileApes (http://www.agileapes.scom/), and
 * associated organization.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 */

package com.agileapes.nemo.util;

import com.agileapes.nemo.contract.Filter;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a utility class for facilitating certain aspects of working with Reflection
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 19:21)
 */
public abstract class ReflectionUtils {

    /**
     * This method will return all methods matching the given filter. The methods will be
     * ordered by precedence so that overriding methods occur sooner in the returned array.
     * @param type            the target class to be scanned for methods
     * @param methodFilter    the filter applied to methods
     * @return an array of matching methods
     */
    public static Method[] getMethods(Class type, Filter<Method> methodFilter) {
        if (type == null || methodFilter == null) {
            throw new NullPointerException();
        }
        final List<Method> methods = new ArrayList<Method>();
        while (type != null) {
            for (Method method : type.getDeclaredMethods()) {
                if (methodFilter.accepts(method)) {
                    methods.add(method);
                }
            }
            type = type.getSuperclass();
        }
        return methods.toArray(new Method[methods.size()]);
    }

    /**
     * Works the same as {@link #getMethods(Class, com.agileapes.nemo.contract.Filter)}
     * only for fields
     * @param type      the type to be introspected
     * @param filter    the filter deciding which fields should remain and which should go
     * @return an array of selected fields
     */
    public static Field[] getFields(Class type, Filter<Field> filter) {
        if (type == null || filter == null) {
            throw new NullPointerException();
        }
        final List<Field> fields = new ArrayList<Field>();
        while (type != null) {
            for (Field field : type.getDeclaredFields()) {
                if (filter.accepts(field)) {
                    fields.add(field);
                }
            }
            type = type.getSuperclass();
        }
        return fields.toArray(new Field[fields.size()]);
    }

    /**
     * @param setterName    the name of the setter method
     * @return the property name for this setter method
     */
    public static String getPropertyName(String setterName) {
        String name = setterName.substring(3);
        name = name.substring(0, 1).toLowerCase() + (name.length() > 1 ? name.substring(1) : "");
        return name;
    }

    private static class Name {

        private String singular;
        private String plural;

        private Name(String singular) {
            this(singular, singular);
        }

        private Name(String singular, String plural) {
            this.singular = singular;
            this.plural = plural;
        }

        private String getSingular() {
            return singular;
        }

        private String getPlural() {
            return plural;
        }
    }

    /**
     * @param type    the type to be described
     * @return a textual, human-friendly description for the given type
     */
    public static String describeType(Class<?> type) {
        int array = 0;
        while (type.isArray()) {
            array ++;
            type = type.getComponentType();
        }
        final Name name;
        if (type.isEnum()) {
            final StringBuilder builder = new StringBuilder();
            Object[] enumConstants = type.getEnumConstants();
            builder.append("one of ");
            for (int i = 0; i < enumConstants.length; i++) {
                Object constant = enumConstants[i];
                builder.append("\"").append(constant.toString().toLowerCase()).append("\"");
                if (i < enumConstants.length - 2) {
                    builder.append(", ");
                } else if (i == enumConstants.length - 2) {
                    builder.append(", or ");
                }
            }
            name = new Name(builder.toString());
        } else if (java.util.Date.class.equals(type) || java.sql.Date.class.equals(type)) {
            name = new Name("a date formatted as: yyyy/mm/dd [hh:mm[:ss]]", "dates formatted as: yyyy/mm/dd [hh:mm[:ss]]");
        } else if (URL.class.equals(type)) {
            name = new Name("a valid URL", "valid URLs");
        } else if (URI.class.equals(type)) {
            name = new Name("a valid URI", "valid URIs");
        } else if (File.class.equals(type)) {
            name = new Name("a path to a file", "paths to files");
        } else if (Class.class.equals(type)) {
            name = new Name("fully qualified name of a class within the classpath (e.g. java.lang.String)",
                    "fully qualified names of classes within the classpath (e.g. java.lang.String)");
        } else {
            String expanded = "";
            final String simpleName = type.getSimpleName();
            for (int i = 0; i < simpleName.length(); i ++) {
                expanded += Character.toLowerCase(simpleName.charAt(i));
                if (Character.isLowerCase(simpleName.charAt(i)) && i < simpleName.length() - 1 && Character.isUpperCase(simpleName.charAt(i + 1))) {
                    expanded += " ";
                }
            }
            final String packageName = type.getName().contains(".") ? type.getName().substring(0, type.getName().lastIndexOf('.')) : "";
            if (!packageName.isEmpty() && !packageName.equals("java.lang")) {
                expanded = '"' + expanded + "\" under \"" + packageName + "\"";
                name = new Name("one " + expanded, "one or more " + expanded);
            } else {
                name = new Name(("aeiou".contains(String.valueOf(expanded.charAt(0))) ? "an " : "a ") + expanded,
                        "one or more " + expanded + "s");
            }
        }
        String value = array > 0 ? name.getPlural() : name.getSingular();
        while (array-- > 0) {
            if (array > 0) {
                value = "arrays of " + value;
            } else {
                value = "an array of " + value;
            }
        }
        return value;
    }

    public static CollectionDSL.Wrapper<Field> withFields(Class type) {
        return CollectionDSL.with(getFields(type, new Filter<Field>() {
            @Override
            public boolean accepts(Field item) {
                return true;
            }
        }));
    }

    public static CollectionDSL.Wrapper<Method> withMethods(Class type) {
        return CollectionDSL.with(getMethods(type, new Filter<Method>() {
            @Override
            public boolean accepts(Method item) {
                return true;
            }
        }));
    }

}
