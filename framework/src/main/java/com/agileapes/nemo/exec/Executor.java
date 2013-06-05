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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The executor is the starting point of the operation chain. You can run your application by
 * instantiating the {@link Executor} inside your {@code main} method:
 * <p><code>public static void main(String[] args) throws Exception {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;new Executor(args).execute();<br/>
 * }</code></p>
 * The same can be achieved using {@link #execute(String...)}:
 * <p><code>public static void main(String[] args) throws Exception {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;Executor.execute(args);<br/>
 * }</code></p>
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 16:35)
 */
public class Executor implements BeanPostProcessor {

    private final Set<Action> actions = new CopyOnWriteArraySet<Action>();
    private Action defaultAction;
    private ApplicationContext context;
    private final String[] args;

    public Executor(String[] args) {
        this.args = args;
    }

    /**
     * @return the set of actions discovered by the framework
     */
    public Set<Action> getActions() {
        return Collections.unmodifiableSet(actions);
    }

    /**
     * @return the default action set by the developer or {@code null} if none are available
     */
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

    /**
     * This method will load the application context and expose the executor via {@link ExecutorAware}
     */
    private void prepareContext() {
        if (this.context != null) {
            throw new IllegalStateException("You cannot execute the same context twice.");
        }
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("nemo/context.xml", "nemo/exec*.xml");
        context.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {
            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                beanFactory.addBeanPostProcessor(Executor.this);
            }
        });
        context.refresh();
        this.context = context;
    }

    /**
     * This internal method will load actions from the Application context
     */
    private void prepareActions() {
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

    /**
     * @return the execution for the current arguments
     */
    public Execution getExecution() {
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
        return new Execution(target, new Options.Builder(Arrays.asList(arguments)).build());
    }

    /**
     * This method will perform the given execution
     * @param execution    an encapsulation of the desired execution
     * @throws Exception
     */
    public void perform(Execution execution) throws Exception {
        final ActionWrapper wrapper = new ActionWrapper(execution.getAction(), context.getBean(ValueReaderContext.class));
        for (String flag : execution.getOptions().getFlags()) {
            wrapper.setFlag(flag);
        }
        for (Map.Entry<String, String> entry : execution.getOptions().getOptions().entrySet()) {
            wrapper.setOption(entry.getKey(), entry.getValue());
        }
        for (String value : execution.getOptions().getIndices()) {
            wrapper.setIndex(value);
        }
        wrapper.perform();
    }

    /**
     * This method will execute the application based on the given arguments. As execution only
     * holds meaning for a single time, calling execute more than once will result in an
     * {@link IllegalStateException}.
     * @throws Exception
     */
    public void execute() throws Exception {
        prepareContext();
        prepareActions();
        perform(getExecution());
    }

    /**
     * This shorthand method is only made available so that by statically importing it
     * your code will look less cluttered.
     * @param args    the arguments to the application as they are
     * @throws Exception
     */
    public static void execute(String ... args) throws Exception {
        new Executor(args).execute();
    }

}
