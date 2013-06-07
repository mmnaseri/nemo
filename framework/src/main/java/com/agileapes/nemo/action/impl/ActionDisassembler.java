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
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The action disassembler will take care of discovering all disassembling strategies found in the
 * classpath through the Spring application context files, and disassemble actions through them.
 *
 * @see #getActionWrapper(Object)
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 18:54)
 */
public class ActionDisassembler implements BeanFactoryPostProcessor {

    public static final Class<? extends DisassembleStrategy> DEFAULT_DISASSEMBLING_STRATEGY = AnnotatedSettersDisassembleStrategy.class;
    private final Set<DisassembleStrategy> disassemblers = new CopyOnWriteArraySet<DisassembleStrategy>();
    private final static Logger logger = Logger.getLogger(ActionDisassembler.class);

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        final String[] names = beanFactory.getBeanNamesForType(DisassembleStrategy.class);
        for (String name : names) {
            if (beanFactory.isSingleton(name)) {
                logger.info("Discovered disassembling strategy " + name);
                disassemblers.add(beanFactory.getBean(name, DisassembleStrategy.class));
            }
        }
    }

    /**
     * This method will find the proper action disassembling strategy for the given action and
     * return an action wrapper that has access to that strategy.
     * @param action    the action
     * @return the wrapper
     */
    public ActionWrapper getActionWrapper(Object action) {
        if (action == null) {
            throw new NullPointerException();
        }
        DisassembleStrategy<?> strategy = null;
        final Class<? extends DisassembleStrategy> targetStrategy;
        boolean specified = false;
        if (action.getClass().isAnnotationPresent(Disassembler.class)) {
            specified = true;
            targetStrategy = action.getClass().getAnnotation(Disassembler.class).value();
        } else {
            logger.info("Falling back to default disassembling strategy: " + DEFAULT_DISASSEMBLING_STRATEGY.getCanonicalName());
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
            if (specified) {
                throw new IllegalStateException("Specified strategy does not recognize action: " + action.getClass().getCanonicalName());
            } else {
                logger.info("Default disassembling strategy does not accept action, looking for a fallback");
                strategy = null;
                for (DisassembleStrategy disassembler : disassemblers) {
                    //noinspection unchecked
                    if (disassembler.accepts(action)) {
                        logger.info("Found fallback strategy: " + disassembler.getClass().getCanonicalName());
                        strategy = disassembler;
                        break;
                    }
                }
                if (strategy == null) {
                    throw new IllegalStateException("No strategy could be found to disassemble action: " + action.getClass().getCanonicalName());
                }
            }
        }
        logger.info("Prepared action wrapper ...");
        //noinspection unchecked
        return new ActionWrapper(action, strategy);
    }

}
