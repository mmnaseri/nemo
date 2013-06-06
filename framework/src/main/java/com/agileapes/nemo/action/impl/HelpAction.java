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
import com.agileapes.nemo.option.Metadata;
import com.agileapes.nemo.option.OptionDescriptor;
import com.agileapes.nemo.util.ReflectionUtils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/6, 19:07)
 */
@Help("Displays help about the given action or the application in general")
public class HelpAction extends Action implements ExecutorAware {

    private String target;
    private String option;
    private Executor executor;

    @Option(index = 0)
    @Help(
            value = "The target action for which help message should be specified",
            description = "By setting this option to '*' you will receive a list of all " +
                    "available actions plus a short description of each, if available.\n" +
                    "You can leave this option unspecified which will mean that you want to " +
                    "see help for the default action."
    )
    public void setTarget(String target) {
        this.target = target;
    }

    @Option(index = 1)
    @Help(
            value = "The option with which you want some help",
            description = "By setting this option to '*' or leaving it unspecified you will " +
                    "receive help for all the available options and flags to the selected target " +
                    "action."
    )
    public void setOption(String option) {
        this.option = option;
    }

    @Override
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void perform(PrintStream output) throws Exception {
        final String helpMetadata = "@" + Help.class.getCanonicalName();
        if (target == null || target.isEmpty() || target.equals("*")) {
            final Action defaultAction = executor.getDefaultAction();
            if (option == null || option.isEmpty()) {
                if (defaultAction == null || (target != null && target.equals("*"))) {
                    final List<Action> actions = new ArrayList<Action>(executor.getActions());
                    Collections.sort(actions);
                    int max = 0;
                    for (Action action : actions) {
                        if (action.isInternal()) {
                            continue;
                        }
                        max = Math.max(max, action.getName().length());
                    }
                    for (Action action : actions) {
                        if (action.isInternal()) {
                            continue;
                        }
                        if (action.isDefaultAction()) {
                            output.print(" * ");
                        } else {
                            output.print("   ");
                        }
                        output.print(action.getName());
                        for (int i = 0; i < max - action.getName().length() + 2; i ++) {
                            output.print(" ");
                        }
                        final Metadata metadata = ((ActionWrapper<?>) action).getMetadata(helpMetadata);
                        if (metadata != null) {
                            output.print(metadata.getProperty("value"));
                        }
                        output.println();
                    }
                } else {
                    target = defaultAction.getName();
                    perform(output);
                }
            } else {
                if (defaultAction == null) {
                    throw new IllegalArgumentException("No default action has been specified");
                }
                target = defaultAction.getName();
                perform(output);
            }
        } else {
            final ActionWrapper<?> action = ((ActionWrapper<?>) executor.getAction(target));
            output.println("Target: " + target);
            if (option == null || option.isEmpty() || option.equals("*")) {
                final List<OptionDescriptor> descriptors = new ArrayList<OptionDescriptor>(action.getOptions());
                Collections.sort(descriptors);
                if (!descriptors.isEmpty()) {
                    output.println("Options:");
                }
                for (OptionDescriptor descriptor : descriptors) {
                    output.print("\t--" + descriptor.getName());
                    if (descriptor.getAlias() != null) {
                        output.print(" (or -" + descriptor.getAlias() + ")");
                    }
                    final Metadata metadata = descriptor.getMetadata(helpMetadata);
                    if (metadata != null && !metadata.getProperty("value").equals("")) {
                        output.print(": " + metadata.getProperty("value"));
                    }
                    output.println();
                }
                if (!action.hasMetadata(helpMetadata)) {
                    output.println("No help has been provided for this action");
                } else {
                    final Metadata metadata = action.getMetadata(helpMetadata);
                    output.println(metadata.getProperty("value"));
                    if (!metadata.getProperty("description").equals("")) {
                        output.println(metadata.getProperty("description"));
                    }
                }
            } else {
                final Set<OptionDescriptor> options = action.getOptions();
                OptionDescriptor descriptor = null;
                for (OptionDescriptor optionDescriptor : options) {
                    if (optionDescriptor.getName().equals(option) || optionDescriptor.getAlias() != null && optionDescriptor.getAlias().toString().equals(option)) {
                        descriptor = optionDescriptor;
                        break;
                    }
                }
                if (descriptor == null) {
                    throw new IllegalArgumentException("No such argument: " + option);
                }
                output.print("Option: --" + descriptor.getName());
                if (descriptor.getAlias() != null) {
                    output.print(" (or -" + descriptor.getAlias() + ")");
                }
                output.println();
                output.print("Usage: --" + descriptor.getName());
                if (descriptor.getType().equals(boolean.class) || descriptor.getType().equals(Boolean.class)) {
                    output.println();
                    output.println("This option is a flag, which means specifying it automatically sets its value to true.");
                    output.println("You can also use '--" + descriptor.getName() + " false' to set its value to false.");
                } else {
                    output.println(" (value)");
                    output.println("Value type: " + ReflectionUtils.getTypeValues(descriptor.getType()));
                }
                if (!descriptor.hasMetadata(helpMetadata)) {
                    output.println("No help has been provided by the developers.");
                } else {
                    final Metadata metadata = descriptor.getMetadata(helpMetadata);
                    output.println(metadata.getProperty("value"));
                    if (!metadata.getProperty("description").equals("")) {
                        output.println(metadata.getProperty("description"));
                    }
                }
            }
        }
    }
}
