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

package com.agileapes.nemo.event;

import com.agileapes.nemo.exec.Executor;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/7, 4:03)
 */
public class ActionNamesListedEvent extends ApplicationEvent {

    private List<String> names;

    /**
     * Create a new ApplicationEvent.
     *
     * @param executor the component that published the event (never {@code null})
     * @param names    the names of the discovererd action
     */
    public ActionNamesListedEvent(Executor executor, List<String> names) {
        super(executor);
        this.names = names;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

}
