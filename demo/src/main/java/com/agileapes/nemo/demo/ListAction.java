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

import java.io.File;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 20:33)
 */
public class ListAction extends Action {

    @Override
    public void perform() throws Exception {
        final File current = new File(".");
        final File[] files = current.listFiles();
        if (files == null) {
            throw new Exception("Could not get the list of files under current directory");
        }
        System.out.println("There are " + files.length + " files under " + current.getAbsolutePath());
        for (File file : files) {
            System.out.println(file.getAbsolutePath());
        }
    }

}
