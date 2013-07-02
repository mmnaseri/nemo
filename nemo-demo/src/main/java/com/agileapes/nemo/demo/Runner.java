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
import com.agileapes.nemo.action.impl.UsageAction;
import com.agileapes.nemo.assets.TypoCorrectionAsset;
import com.agileapes.nemo.exec.ExecutorContext;
import com.agileapes.nemo.util.ExceptionMessage;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static com.agileapes.nemo.exec.ExecutorContext.withActions;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 20:33)
 */
public class Runner {

    public static void main(String[] args) throws Exception {
        try {
//            to do this without spring:
//            (
//                    (ExecutorContext) withActions(UsageAction.class, HelloAction.class, HelpAction.class, ReadAction.class, ListAction.class)
//                    .addEventListener(new TypoCorrectionAsset(0.3))
//                    .addEventListener(new ReadActionAlias())
//            )
//                    .execute(args);
            final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("nemo/execution.xml");
            applicationContext.getBean(ExecutorContext.class).execute(args);
        } catch (Throwable e) {
            System.err.println("error: " + new ExceptionMessage(e));
            e.printStackTrace();
            System.exit(1);
        }
    }

}
