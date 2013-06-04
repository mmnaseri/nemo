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

import com.agileapes.nemo.api.Option;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 18:27)
 */
public class NemoAction extends Action {

    private boolean verbose;

    public boolean isVerbose() {
        return verbose;
    }

    @Option(alias = 'v')
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void perform() throws Exception {
        System.out.println(" Nemo v1.0 -- CLI assistance framework");
        System.out.println("========================================");
        System.out.println("  brought to you by AgileApes, Ltd.");
        System.out.println("  http://projects.agileapes.com/nemo");
        System.out.println("  Copyright AgileApes, Ltd (c) 2013");
        if (verbose) {
            System.out.println("----------------------------------------");
            System.out.println("  Developed by: M. M. Naseri");
            System.out.println("                m.m.naseri@gmail.com");
        }
    }

}
