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

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/5, 13:01)
 */
@Help(
        value = "Helps you understand how the application works",
        description = "Use '--target' to specify which target you want help with.\n" +
                "The same applies to '--option' for specifying the exact option you " +
                "want to know more about.\n" +
                "Invoking this action without any arguments will list all targets plus " +
                "a short description of what each will do."
)
public class HelpAction extends Action implements ExecutorAware {

    private Executor executor;
    private String target;
    private String option;

    @Override
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Option(index = 0)
    @Help("The target for which you want more help")
    public void setTarget(String target) {
        this.target = target;
    }

    @Option(index = 1)
    @Help("The option for which you want more help")
    public void setOption(String option) {
        this.option = option;
    }

    @Override
    public void perform(PrintStream output) throws Exception {
        if (target != null && !target.isEmpty()) {
            final Action action = executor.getAction(target);
            if (option == null || option.isEmpty()) {
                if (action.getClass().isAnnotationPresent(Help.class)) {
                    final Help help = action.getClass().getAnnotation(Help.class);
                    output.println("Target: " + target);
                    output.println(help.value());
                    if (!help.description().isEmpty()) {
                        output.println(help.description());
                    }
                } else {
                    output.println(target + ": No value is available for this target");
                }
            } else {
                final Method method = new ActionWrapper(action, null).getSetter(option);
                if (method.isAnnotationPresent(Help.class)) {
                    final Help help = method.getAnnotation(Help.class);
                    output.println(target + " -" + (option.length() > 1 ? "-" : "") + option + " (value)");
                    output.println(help.value());
                    if (!help.description().isEmpty()) {
                        output.println(help.description());
                    }
                } else {
                    output.println(target + "." + option + ": No value is available for this option");
                }
            }
        } else {
            if (option == null || option.isEmpty()) {
                final Set<Action> actions = executor.getActions();
                final List<String> names = new ArrayList<String>();
                int max = 0;
                for (Action action : actions) {
                    names.add(action.getName());
                    max = Math.max(max, action.getName().length());
                }
                Collections.sort(names);
                for (String name : names) {
                    output.print(name);
                    for (int i = 0; i < max - name.length() + 4; i++) {
                        output.print(" ");
                    }
                    final Class<? extends Action> actionClass = executor.getAction(name).getClass();
                    if (actionClass.isAnnotationPresent(Help.class)) {
                        output.println(actionClass.getAnnotation(Help.class).value());
                    } else {
                        output.println();
                    }
                }
            } else {
                final Action action = executor.getDefaultAction();
                if (action == null) {
                    throw new IllegalArgumentException("No default action set for the application");
                }
                target = action.getName();
                perform(output);
            }
        }
    }

}
