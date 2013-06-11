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

import java.io.File;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 20:33)
 */
@Help(value = "Lists the files in the current directory")
public class ListAction extends Action {

    @Option(alias = 'x')
    @Help(value = "Prints sequential file number prior to file path")
    private boolean numbers;

    @Override
    public void execute() throws Exception {
        final File current = new File(".");
        final File[] files = current.listFiles();
        if (files == null) {
            throw new Exception("Could not get the list of files under current directory");
        }
        int i = 0;
        output.println("There are " + files.length + " files under " + current.getAbsolutePath());
        for (File file : files) {
            if (numbers) {
                output.print((++i) + "\t");
            }
            output.print(file.length());
            output.print("\t ");
            output.println(file.getName());
        }
    }
}
