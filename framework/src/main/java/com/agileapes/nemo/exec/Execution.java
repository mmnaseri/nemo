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

package com.agileapes.nemo.exec;

import com.agileapes.nemo.action.Action;

import java.util.Arrays;
import java.util.List;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 18:18)
 */
public class Execution {

    private final Action action;
    private final List<String> arguments;

    public Execution(Action action, String... arguments) {
        this(action, Arrays.asList(arguments));
    }

    public Execution(Action action, List<String> arguments) {
        this.action = action;
        this.arguments = arguments;
    }

    Action getAction() {
        return action;
    }

    List<String> getArguments() {
        return arguments;
    }

}
