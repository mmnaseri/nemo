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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * This value reader will take URLs and URIs and read them from their textual representations.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/5, 15:42)
 */
public class UrlValueReader implements ValueReader {

    @Override
    public boolean handles(Class<?> type) {
        return URL.class.equals(type) || URI.class.equals(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E read(String text, Class<E> type) {
        if (type.equals(URL.class)) {
            try {
                return (E) new URL(text);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Malformed URL entered: " + text, e);
            }
        } else if (type.equals(URI.class)) {
            return (E) URI.create(text);
        }
        throw new IllegalArgumentException();
    }

}
