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
 * The reader context is designed as a repository of value readers that can handle all
 * the types which can be handled by any of the readers by delegating the task of reading
 * the values
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 18:59)
 */
public interface ValueReaderContext extends ValueReader {

    /**
     * Will add the given reader to the repository
     * @param reader    the value reader to be added
     */
    void add(ValueReader reader);

}
