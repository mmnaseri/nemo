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

package com.agileapes.nemo.action.impl;

import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.api.Help;
import com.agileapes.nemo.api.Option;
import com.agileapes.nemo.exec.Executor;
import com.agileapes.nemo.exec.ExecutorAware;
import com.agileapes.nemo.option.OptionDescriptor;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/6, 18:11)
 */
@Help(
        value = "Displays usage information for the application",
        description = "You can specify a target to see its options and usage information.\n" +
                "Here, '%APPLICATION%' means the specified command you used to invoke this " +
                "application."
)
public class UsageAction extends Action implements ExecutorAware {

    private String target;
    private Executor executor;

    @Option(index = 0)
    @Help(
            value = "The target for which usage information will be displayed",
            description = "In this mode, optional arguments will be displayed in a pair of " +
                    "square brackets and boolean flags will appear without a following value"
    )
    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void perform(PrintStream output) throws Exception {
        output.print("%APPLICATION% ");
        if (target == null) {
            final List<Action> actions = new ArrayList<Action>(executor.getActions());
            Collections.sort(actions);
            for (Iterator<Action> iterator = actions.iterator(); iterator.hasNext(); ) {
                final Action action = iterator.next();
                output.print(action.getName());
                if (iterator.hasNext()) {
                    output.print("|");
                }
            }
            output.println(" [action options]");
        } else {
            output.print(target);
            output.print(" ");
            final Action action = executor.getAction(target);
            final List<OptionDescriptor> descriptors = new ArrayList<OptionDescriptor>(((ActionWrapper<?>) action).getOptions());
            Collections.sort(descriptors);
            for (Iterator<OptionDescriptor> iterator = descriptors.iterator(); iterator.hasNext(); ) {
                final OptionDescriptor descriptor = iterator.next();
                output.print(descriptor.isRequired() ? "" : "[");
                output.print("--");
                output.print(descriptor.getName());
                if (descriptor.getAlias() != null) {
                    output.print("|-");
                    output.print(descriptor.getAlias());
                }
                if (!descriptor.getType().equals(boolean.class) && !descriptor.getType().equals(Boolean.class)) {
                    output.print(" (value)");
                }
                output.print(descriptor.isRequired() ? "" : "]");
                if (iterator.hasNext()) {
                    output.print(" ");
                }
            }
            output.println();
        }
    }
}
