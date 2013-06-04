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

package com.agileapes.nemo.action;

import com.agileapes.nemo.exec.Executor;
import com.agileapes.nemo.exec.ExecutorAware;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 19:35)
 */
public class UsageAction extends Action implements ExecutorAware {

    private Executor executor;

    @Override
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void perform() throws Exception {
        final Set<Action> actions = executor.getActions();
        final StringBuilder builder = new StringBuilder("Usage: %APPLICATION% ");
        for (Iterator<Action> iterator = actions.iterator(); iterator.hasNext(); ) {
            builder.append(iterator.next().getName());
            if (iterator.hasNext()) {
                builder.append("|");
            }
        }
        builder.append(" [target options]");
        System.out.println(builder.toString());
    }

}
