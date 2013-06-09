package com.agileapes.nemo.exec;

import com.agileapes.nemo.action.Action;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/9, 16:41)
 */
public class ExecutorBuilder {

    public static final String ACTION = "Action";
    private static final Logger logger = Logger.getLogger(ExecutorBuilder.class);
    private final DefaultListableBeanFactory beanFactory;
    private final StopWatch stopWatch;

    public ExecutorBuilder() {
        beanFactory = new DefaultListableBeanFactory();
        stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("Started indirect bootstrapping");
    }

    private String getName(Object action) {
        String name = action.getClass().getSimpleName();
        if (name.endsWith(ACTION)) {
            name = name.substring(0, name.length() - ACTION.length());
        }
        name = StringUtils.uncapitalize(name);
        return name;
    }

    /**
     * This method will add the <em>pre-configured</em> action {@code action}
     * with name {@code name} into the to–be-built executor.
     * @param name      the target name for this action
     * @param action    the action
     * @return the
     */
    public ExecutorBuilder add(String name, Object action) {
        logger.info("Registering action " + name);
        beanFactory.registerSingleton(name, action);
        return this;
    }

    public ExecutorBuilder add(Object action) {
        return add(getName(action), action);
    }

    public ExecutorBuilder addDefault(String name, Action action) {
        action.setDefaultAction(true);
        return add(name, action);
    }

    public ExecutorBuilder addDefault(Action action) {
        return addDefault(getName(action), action);
    }

    public ExecutorBuilder addInternal(String name, Action action) {
        action.setInternal(true);
        return add(name, action);
    }

    public ExecutorBuilder addInternal(Action action) {
        return addInternal(getName(action), action);
    }

    public Executor build() throws Exception {
        final GenericApplicationContext context = new GenericApplicationContext(beanFactory);
        context.refresh();
        final Executor executor = Bootstrap.load(context);
        stopWatch.stop();
        logger.info("Indirect bootstrapping took " + stopWatch.getTotalTimeMillis() + "ms");
        return executor;
    }

}
