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

package com.agileapes.nemo.demo;

import com.agileapes.nemo.action.impl.HelpAction;
import com.agileapes.nemo.action.impl.NemoAction;
import com.agileapes.nemo.action.impl.UsageAction;
import com.agileapes.nemo.exec.ExecutorBuilder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 20:33)
 */
public class Runner {

    public static void main(String[] args) throws Exception {
        Logger.getLogger("com.agileapes.nemo").setLevel(Level.OFF);
        try {
            new ExecutorBuilder()
                    .add("about", new NemoAction())
                    .add(new ListAction())
                    .add(new ReadAction())
                    .add(new HelpAction())
                    .addDefault(new UsageAction())
                    .build().execute(args);
        } catch (Throwable e) {
            System.err.println("error: " + e.getMessage());
            System.exit(1);
        }
    }

}