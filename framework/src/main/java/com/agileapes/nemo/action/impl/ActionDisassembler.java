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

import com.agileapes.nemo.action.DisassembleStrategy;
import com.agileapes.nemo.api.Disassembler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The action wrapper carries the duty of converting String values to object values using
 * {@link com.agileapes.nemo.value.ValueReader}s. This class is basically the glue which
 * holds the underlying system together.
 * 
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 18:54)
 */
public class ActionDisassembler implements BeanFactoryPostProcessor {

    public static final Class<? extends DisassembleStrategy> DEFAULT_DISASSEMBLING_STRATEGY = AnnotatedSettersDisassembleStrategy.class;
    private final Set<DisassembleStrategy> disassemblers = new CopyOnWriteArraySet<DisassembleStrategy>();

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        final String[] names = beanFactory.getBeanNamesForType(DisassembleStrategy.class);
        for (String name : names) {
            if (beanFactory.isSingleton(name)) {
                final DisassembleStrategy bean = beanFactory.getBean(name, DisassembleStrategy.class);
                disassemblers.add(bean);
            }
        }
    }

    public ActionWrapper getActionWrapper(Object action) {
        if (action == null) {
            throw new NullPointerException();
        }
        DisassembleStrategy<?> strategy = null;
        final Class<? extends DisassembleStrategy> targetStrategy;
        if (action.getClass().isAnnotationPresent(Disassembler.class)) {
            targetStrategy = action.getClass().getAnnotation(Disassembler.class).value();
        } else {
            targetStrategy = DEFAULT_DISASSEMBLING_STRATEGY;
        }
        for (DisassembleStrategy disassembler : disassemblers) {
            if (disassembler.getClass().equals(targetStrategy)) {
                strategy = disassembler;
                break;
            }
        }
        if (strategy == null) {
            throw new IllegalStateException("Disassembler not found: " + targetStrategy.getCanonicalName());
        }
        if (!strategy.accepts(action)) {
            throw new IllegalStateException("Specified strategy does not recognize action: " + action.getClass().getCanonicalName());
        }
        //noinspection unchecked
        return new ActionWrapper(action, strategy);
    }

}
