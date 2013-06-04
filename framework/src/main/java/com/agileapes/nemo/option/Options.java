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

package com.agileapes.nemo.option;

import java.util.*;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 16:35)
 */
public class Options {

    private final Map<String, String> options = new HashMap<String, String>();
    private final Set<String> flags = new HashSet<String>();

    private Options() {}

    private void setFlag(String flag) {
        flags.add(flag);
    }

    private void setOption(String name, String value) {
        options.put(name, value);
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public Set<String> getFlags() {
        return flags;
    }

    public static class Builder {

        private final List<String> arguments;

        public Builder(List<String> arguments) {
            this.arguments = arguments;
        }

        public Options build() {
            final Options options = new Options();
            for (int i = 0; i < arguments.size(); i++) {
                final String argument = arguments.get(i);
                if (argument.startsWith("--")) {
                    if (argument.length() == 2) {
                        throw new IllegalArgumentException("Invalid command-line argument: " + argument);
                    }
                    if (i == arguments.size() - 1 || arguments.get(i + 1).startsWith("-")) {
                        options.setFlag(argument.substring(2));
                    } else {
                        options.setOption(argument.substring(2), arguments.get(i + 1));
                        i ++;
                    }
                } else if (argument.startsWith("-")) {
                    if (argument.length() == 1) {
                        throw new IllegalArgumentException("Invalid command-line argument: " + argument);
                    }
                    if (i == arguments.size() - 1 || arguments.get(i + 1).startsWith("-")) {
                        for (int j = 1; j < argument.length(); j ++) {
                            options.setFlag(argument.substring(j, j + 1));
                        }
                    } else {
                        options.setOption(argument.substring(1), arguments.get(i + 1));
                        i ++;
                    }
                } else {
                    throw new IllegalArgumentException("Invalid command-line argument: " + argument);
                }
            }
            return options;
        }

    }

}
