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

package com.agileapes.nemo.value.impl;

import com.agileapes.nemo.value.ValueReader;

/**
 * This value reader will handle all 7 primitive types within Java (and their object-oriented
 * counterparts) plus the String data type.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 18:59)
 */
public class PrimitiveValueReader implements ValueReader {
    @Override
    public Class[] getTypes() {
        return new Class[]{int.class, long.class, short.class, boolean.class, char.class,
            float.class, double.class, Integer.class, Long.class, Short.class, Boolean.class,
            Character.class, Float.class, Double.class, String.class};
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E read(String text, Class<E> type) {
        if (text.equals("null")) {
            return null;
        }
        if (text.equals("\\null")) {
            text = "null";
        }
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return (E) ((Integer) Integer.parseInt(text));
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            return (E) ((Long) Long.parseLong(text));
        } else if (type.equals(short.class) || type.equals(Short.class)) {
            return (E) ((Short) Short.parseShort(text));
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return (E) ((Boolean) Boolean.parseBoolean(text));
        } else if (type.equals(char.class) || type.equals(Character.class)) {
            if (text.length() != 1) {
                throw new IllegalArgumentException(text + " is not a valid character");
            }
            return (E) ((Character) text.charAt(0));
        } else if (type.equals(float.class) || type.equals(Float.class)) {
            return (E) ((Float) Float.parseFloat(text));
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            return (E) ((Double) Double.parseDouble(text));
        } else if (type.equals(String.class)) {
            return (E) text;
        }
        throw new IllegalArgumentException(text + " is not a valid input for " + getClass().getSimpleName());
    }
}
