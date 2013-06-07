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

package com.agileapes.nemo.value;

/**
 * ValueReaders are a way for the framework to read values from their String representation
 * and return the actual type object.
 *
 * For value readers to be recognized and picked up by the framework, you will have to add
 * them as singleton beans to Spring's application context under {@code /nemo/*.xml}.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 18:57)
 */
public interface ValueReader {

    /**
     * @return {@code true} if the given type is handled by this class
     * @param type    the type being processed
     */
    boolean handles(Class<?> type);

    /**
     * @param text    the textual representation of the data
     * @param type    the desired type of the data
     * @param <E>     actual data type parameter
     * @return the converted value
     */
    <E> E read(String text, Class<E> type);

}
