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
import com.agileapes.nemo.action.impl.ActionDisassembler;
import com.agileapes.nemo.action.impl.ActionWrapper;
import com.agileapes.nemo.api.Disassembler;
import com.agileapes.nemo.event.*;
import com.agileapes.nemo.option.Options;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The executor is the starting point of the operation chain. You can run your application by
 * instantiating the {@link Executor} inside your {@code main} method:
 * <p><code>public static void main(String[] args) throws Exception {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;new Executor(args).execute();<br/>
 * }</code></p>
 * The same can be achieved using {@link #execute(java.io.PrintStream, String...)}:
 * <p><code>public static void main(String[] args) throws Exception {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;Executor.execute(args);<br/>
 * }</code></p>
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 16:35)
 */
public class Executor implements BeanFactoryPostProcessor, ApplicationContextAware {

    private final static Logger logger = Logger.getLogger(Executor.class);
    private final Set<Action> actions = new CopyOnWriteArraySet<Action>();
    private Action defaultAction;
    private ApplicationContext context;
    private String[] args;
    private PrintStream output;
    private Execution execution = null;
    private Multicaster multicaster;
    private String defaultActionName;

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
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        final String[] names = beanFactory.getBeanNamesForType(ExecutorAware.class);
        for (String name : names) {
            if (!beanFactory.isSingleton(name)) {
                logger.info("Refusing service to non-singleton bean: " + name);
                continue;
            }
            beanFactory.getBean(name, ExecutorAware.class).setExecutor(this);
        }
    }

    /**
     * This method will load the application context and expose the executor via {@link ExecutorAware}
     */
    private void prepareContext() {
        logger.info("Preparing fallback application context");
        logger.warn("This method is not recommended, as it is much slower. Use " + Bootstrap.class.getCanonicalName() + ".load() instead");
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("nemo/nemo.xml", "nemo/exec*.xml");
        context.addBeanFactoryPostProcessor(this);
        context.refresh();
        this.context = context;
    }

    /**
     * This internal method will load actions from the Application context
     */
    private void prepareActions() {
        logger.info("Preparing to load actions");
        List<String> names = new ArrayList<String>();
        Collections.addAll(names, context.getBeanNamesForType(Action.class));
        logger.info("Discovered " + names.size() + " actions extending " + Action.class);
        final String[] definitionNames = context.getBeanDefinitionNames();
        for (String name : definitionNames) {
            if (names.contains(name)) {
                continue;
            }
            if (context.getType(name).isAnnotationPresent(Disassembler.class)) {
                logger.info("Discovered bean " + name + " additionally through the @Disassembler annotation");
                names.add(name);
            }
        }
        names = multicaster.publishEvent(new ActionNamesListedEvent(this, names)).getNames();
        final ActionDisassembler disassembler = context.getBean(ActionDisassembler.class);
        for (String name : names) {
            if (context.isSingleton(name)) {
                logger.debug("Wrapping action " + name + " ...");
                final Action action = disassembler.getActionWrapper(context.getBean(name));
                action.setBeanName(name);
                if (action.getName().equals(defaultActionName)) {
                    action.setDefaultAction(true);
                }
                if (action.isDefaultAction()) {
                    logger.debug("Action " + name + " marked as default");
                    if (action.isInternal()) {
                        logger.error("Action " + name + " cannot be both internal and default");
                        throw new IllegalStateException("Internal actions cannot be marked as the default action");
                    }
                    if (defaultAction == null) {
                        defaultAction = action;
                    } else {
                        logger.error("Action " + defaultAction.getName() + " already marked as default");
                        throw new IllegalStateException("More than one default action specified");
                    }
                }
                actions.add(action);
            }
        }
        final Set<Action> actionSet = multicaster.publishEvent(new ActionsPreparedEvent(this, actions)).getActions();
        if (!actions.equals(actionSet)) {
            logger.info("List of actions has been updated");
            actions.clear();
            actions.addAll(actionSet);
        }
    }

    /**
     * This will select the named action from the context
     * @param target    the name of the action
     * @return the prepared instance of the action
     */
    public Action getAction(String target) {
        if (target == null || target.isEmpty()) {
            return getDefaultAction();
        }
        for (Action action : actions) {
            if (action.getName().equals(target)) {
                return action;
            }
        }
        throw new IllegalStateException("Invalid target: " + args[0]);
    }

    /**
     * @return the execution for the current arguments
     */
    public Execution getExecution() {
        if (execution != null) {
            return execution;
        }
        logger.info("Execution is being configured");
        final Action target;
        String[] arguments;
        if (args.length == 0 || args[0].startsWith("-")) {
            logger.info("Falling back to the default action");
            if (defaultAction == null) {
                throw new IllegalStateException("No default action specified");
            }
            arguments = args;
            target = defaultAction;
        } else {
            logger.info("Target action chosen: " + args[0]);
            target = getAction(args[0]);
            arguments = new String[args.length - 1];
            System.arraycopy(args, 1, arguments, 0, args.length - 1);
        }
        if (target.isInternal()) {
            throw new IllegalStateException("Internal actions cannot be invoked from the command-line");
        }
        execution = new Execution(target, new Options.Builder(Arrays.asList(arguments)).build());
        execution = multicaster.publishEvent(new ExecutionConfiguredEvent(this, execution)).getExecution();
        return execution;
    }

    /**
     * This method will perform the given execution
     * @param execution    an encapsulation of the desired execution
     * @throws Exception
     */
    public void perform(Execution execution) throws Exception {
        logger.info("Preparing action to be performed");
        final Action action = execution.getAction();
        final Options options = execution.getOptions();
        logger.info("Resetting action options");
        ((ActionWrapper) action).reset();
        for (Map.Entry<String, String> entry : options.getOptions().entrySet()) {
            logger.debug("Setting option " + entry.getKey() + " to " + entry.getValue());
            ((ActionWrapper) action).setOption(entry.getKey(), entry.getValue());
        }
        for (String flag : options.getFlags()) {
            logger.debug("Enabling flag " + flag);
            ((ActionWrapper) action).setFlag(flag);
        }
        final List<String> indices = options.getIndices();
        for (int i = 0; i < indices.size(); i++) {
            logger.debug("Setting option based on index number " + i);
            final String value = indices.get(i);
            ((ActionWrapper) action).setIndex(i, value);
        }
        logger.info("Performing delegate action " + action.getName());
        multicaster.publishEvent(new BeforeActionPerformedEvent(this, action));
        action.perform(output);
        multicaster.publishEvent(new AfterActionPerformedEvent(this, action));
    }

    void setDefaultActionName(String defaultActionName) {
        this.defaultActionName = defaultActionName;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    /**
     * This method will execute the application based on the given arguments. As execution only
     * holds meaning for a single time, calling execute more than once will result in an
     * {@link IllegalStateException}.
     * @throws Exception
     */
    public void execute(String... args) throws Exception {
        logger.info("Redirecting output to the standard output");
        execute(System.out, args);
    }

    /**
     * This method will execute the application based on the given arguments. As execution only
     * holds meaning for a single time, calling execute more than once will result in an
     * {@link IllegalStateException}.
     * @throws Exception
     */
    public void execute(PrintStream output, String... args) throws Exception {
        this.output = output;
        this.args = args;
        if (context == null) {
            prepareContext();
        }
        logger.info("Starting up ...");
        this.multicaster = new Multicaster(context);
        prepareActions();
        perform(getExecution());
        multicaster.publishEvent(new ApplicationShutdownEvent(this));
    }
}
