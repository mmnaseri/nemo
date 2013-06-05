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

import com.agileapes.nemo.api.Help;
import com.agileapes.nemo.api.Option;
import com.agileapes.nemo.exec.Executor;
import com.agileapes.nemo.exec.ExecutorAware;

import java.util.Iterator;
import java.util.Set;

/**
 * This action will simply list all the possible targets to this application
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 19:35)
 */
@Help(
        value = "This action will value you identify what the application is capable of doing",
        description = "Use '--target' to get more detailed information on a specific target"
)
public class UsageAction extends Action implements ExecutorAware {

    private Executor executor;
    private String target;

    @Override
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Option(alias = 't', index = 0)
    @Help(
            value = "This will identify the different options available to each target",
            description = "Options can have aliases, which will be separated by a '|' and " +
                    "they also might not be required at all, in which case they will be " +
                    "encased in a pair of square brackets."
    )
    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public void perform() throws Exception {
        final StringBuilder builder = new StringBuilder("Usage:\n");
        builder.append("%APPLICATION% ");
        if (target == null) {
            final Set<Action> actions = executor.getActions();
            for (Iterator<Action> iterator = actions.iterator(); iterator.hasNext(); ) {
                builder.append(iterator.next().getName());
                if (iterator.hasNext()) {
                    builder.append("|");
                }
            }
            builder.append(" [target options]");
        } else {
            builder.append(target).append(" ");
            Action targetAction = executor.getAction(target);
            final ActionWrapper wrapper = new ActionWrapper(targetAction, null);
            for (Iterator<String> iterator = wrapper.getSetters().keySet().iterator(); iterator.hasNext(); ) {
                String property = iterator.next();
                final boolean required = wrapper.getSetters().get(property).getAnnotation(Option.class).required();
                if (!required) {
                    builder.append("[");
                }
                builder.append("--").append(property);
                final char alias = wrapper.getSetters().get(property).getAnnotation(Option.class).alias();
                if (alias != ' ') {
                    builder.append("|").append("-").append(alias);
                }
                builder.append(" ");
                builder.append("(value)");
                if (!required) {
                    builder.append("]");
                }
                if (iterator.hasNext()) {
                    builder.append(" ");
                }
            }

        }
        System.out.println(builder.toString());
    }

}
