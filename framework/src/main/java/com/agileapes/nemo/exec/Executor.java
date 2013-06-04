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

package com.agileapes.nemo.exec;

import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.action.ActionWrapper;
import com.agileapes.nemo.api.Default;
import com.agileapes.nemo.option.Options;
import com.agileapes.nemo.value.ValueReaderContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 16:35)
 */
public class Executor implements BeanPostProcessor {

    private final Set<Action> actions = new CopyOnWriteArraySet<Action>();
    private Action defaultAction;
    private ApplicationContext context;

    public Set<Action> getActions() {
        return Collections.unmodifiableSet(actions);
    }

    public Action getDefaultAction() {
        return defaultAction;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ExecutorAware) {
            ((ExecutorAware) bean).setExecutor(this);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    private ApplicationContext prepareContext() {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("nemo/*.xml");
        context.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {
            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                beanFactory.addBeanPostProcessor(Executor.this);
            }
        });
        context.refresh();
        return context;
    }

    private void prepareActions(ApplicationContext context) {
        final String[] names = context.getBeanNamesForType(Action.class);
        for (String name : names) {
            if (context.isSingleton(name)) {
                final Action bean = context.getBean(name, Action.class);
                if (bean.getClass().isAnnotationPresent(Default.class)) {
                    if (defaultAction == null) {
                        defaultAction = bean;
                    } else {
                        throw new IllegalStateException("More than one default action specified");
                    }
                }
                actions.add(bean);
            }
        }
    }

    public void execute(String ... args) throws Exception {
        if (context != null) {
            throw new IllegalStateException("You cannot execute the same context twice.");
        }
        context = prepareContext();
        prepareActions(context);
        Action target = null;
        String[] arguments;
        if (args.length == 0 || args[0].startsWith("-")) {
            if (defaultAction == null) {
                throw new IllegalStateException("No default action specified");
            }
            arguments = args;
            target = defaultAction;
        } else {
            for (Action action : actions) {
                if (action.getName().equals(args[0])) {
                    target = action;
                    break;
                }
            }
            if (target == null) {
                throw new IllegalStateException("Invalid target: " + args[0]);
            }
            arguments = new String[args.length - 1];
            System.arraycopy(args, 1, arguments, 0, args.length - 1);
        }
        perform(new Execution(target, arguments));
    }

    public void perform(Execution execution) throws Exception {
        final Options options = new Options.Builder(execution.getArguments()).build();
        final ActionWrapper wrapper = new ActionWrapper(execution.getAction(), context.getBean(ValueReaderContext.class));
        for (String flag : options.getFlags()) {
            wrapper.setFlag(flag);
        }
        for (Map.Entry<String, String> entry : options.getOptions().entrySet()) {
            wrapper.setOption(entry.getKey(), entry.getValue());
        }
        wrapper.perform();
    }

}
