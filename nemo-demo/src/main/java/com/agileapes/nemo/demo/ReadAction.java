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

import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.api.Help;
import com.agileapes.nemo.api.Option;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 20:35)
 */
@Help(
        value = "Prints out the contents of the specified file",
        description = "Use '--file' to specify which file should be used"
)
public class ReadAction extends Action {

    private File file;
    private int limit;

    @Option(alias = 'f', required = true, index = 0)
    public void setFile(File file) {
        this.file = file;
    }

    @Option(alias = 'l')
    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public void perform(PrintStream output) throws Exception {
        if (limit > 0) {
            output.println("Reading the first " + limit + " line(s) of " + file.getAbsolutePath());
        } else {
            output.println("Reading the contents of: " + file.getAbsolutePath());
        }
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        int i = 0;
        while ((line = reader.readLine()) != null) {
            output.println((++ i) + "\t" + line);
            if (limit > 0 && i >= limit) {
                output.println("\t--> Adjust the limit to see the rest of this file.");
                break;
            }
        }
        reader.close();
    }

}
