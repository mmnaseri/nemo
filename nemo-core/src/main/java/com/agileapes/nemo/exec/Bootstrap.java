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

import com.agileapes.nemo.event.Multicaster;
import com.agileapes.nemo.event.NemoBootstrappedEvent;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Bootstrap class will offer an alternate to loading beans via
 * Spring's heavy mechanism. This way, the few beans instantiated automatically
 * by Nemo need not occupy their own application context XML document.
 *
 * This offers as much as 4x faster startup.
 *
 * @see #load(ConfigurableApplicationContext)
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/7, 3:03)
 */
@SuppressWarnings("UnusedDeclaration")
public class Bootstrap {

    public static final String NEMO_EXECUTOR = "nemoExecutor";
    public static final String NEMO_EXECUTION_DEFAULT = "/nemo/execution.xml";
    private static final Logger logger = Logger.getLogger(Bootstrap.class);

    /**
     * This method loads the application context file for nemo from the
     * classpath and returns its content
     * @param name    the path to the context file
     * @return the content of the application context file
     * @throws IOException
     */
    private static String getContextContent(String name) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(Bootstrap.class.getResourceAsStream(name)));
        final StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append('\n');
        }
        reader.close();
        return stringBuilder.toString();
    }

    /**
     * This method will generate a map from bean IDs to their corresponding
     * bean types as defined in nemo's main application context file.
     * @param content the contents of the application context file
     * @return a map of beans
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static Map<String, Class<?>> getBeansDefinitions(String content) throws IOException, ClassNotFoundException {
        final Matcher matcher = Pattern.compile("<bean(?:\\s+(?:id|name)=\"(.*?)\")?\\s+class=\"(.*?)\"", Pattern.MULTILINE | Pattern.DOTALL).matcher(content);
        final Map<String, Class<?>> beans = new HashMap<String, Class<?>>();
        while (matcher.find()) {
            String id = matcher.group(1);
            final Class<?> type = ClassUtils.forName(matcher.group(2), Bootstrap.class.getClassLoader());
            id = id == null ? StringUtils.uncapitalize(type.getSimpleName()) : id;
            beans.put(id, type);
        }
        return beans;
    }

    /**
     * This method will create a {@link StaticApplicationContext} instance
     * which is a static, faster way of maintaining an application context.
     *
     * This way, all the goodness that is an application context is kept
     * in place while we do not initialize a full-blown application context
     * file.
     *
     * @return an instance of {@link Executor} which can be used to access
     * nemo's core functionality
     * @throws Exception
     */
    public static Executor load(ConfigurableApplicationContext applicationContext) throws Exception{
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("Building nemo backbone context configuration");
        final StaticApplicationContext context = new StaticApplicationContext();
        //this is for the sake of proper order of initialization
        applicationContext.setParent(context);
        //this is so that getBean looks up beans from the static context, too
        try {
            applicationContext.getBeanFactory().setParentBeanFactory(context);
        } catch (IllegalStateException ignored) {}
        final Map<String, Class<?>> map = getBeansDefinitions(getContextContent("/nemo/nemo.xml"));
        map.put(NEMO_EXECUTOR, Executor.class);
        for (Map.Entry<String, Class<?>> entry : map.entrySet()) {
            logger.debug("Adding singleton bean of type <" + entry.getValue().getCanonicalName() + "> with unique identifier: " + entry.getKey());
            context.registerSingleton(entry.getKey(), entry.getValue());
        }
        context.refresh();
        final ConfigurableApplicationContext bootstrappedContext = new Multicaster(applicationContext).publishEvent(new NemoBootstrappedEvent(applicationContext)).getApplicationContext();
        logger.info("Context config file bootstrapped, dispensing executor");
        final Executor executor = applicationContext.getBean(Executor.class);
        executor.setApplicationContext(bootstrappedContext);
        //this processes beans that require IoC for proper functioning
        final Map<String, BeanFactoryPostProcessor> processors = context.getBeansOfType(BeanFactoryPostProcessor.class);
        for (BeanFactoryPostProcessor processor : processors.values()) {
            processor.postProcessBeanFactory(applicationContext.getBeanFactory());
        }
        stopWatch.stop();
        logger.info("Bootstrapping took " + stopWatch.getTotalTimeMillis() + "ms");
        return executor;
    }

    /**
     * This is the same as {@link #load(ConfigurableApplicationContext)} but it instantiates
     * its own application context, assuming that the configuration for the application is available under {@code /nemo/execution.xml},
     * otherwise it will raise a {@link FileNotFoundException}.
     * @return the executor instantiated by the bootstrapping mechanism
     * @throws Exception
     * @see #load(ConfigurableApplicationContext)
     */
    public static Executor load() throws Exception {
        logger.info("Loading execution context from predesignated file :" + NEMO_EXECUTION_DEFAULT);
        final URL resource = Bootstrap.class.getResource(NEMO_EXECUTION_DEFAULT);
        if (resource == null) {
            throw new FileNotFoundException("File " + NEMO_EXECUTION_DEFAULT + " not found under the running module.");
        }
        return load(new FileSystemXmlApplicationContext(resource.toExternalForm()));
    }

    /**
     * This method statically reads bean class and identifiers from the specified XML
     * configuration file.
     * <strong>NB</strong> this method does not replace Spring's XML application context
     * configuration loader, as it is meant to be just a fast approach for replacing the core
     * of the functionality. In comparison, this method performs much faster, while it
     * completely ignores constructor parameters, setter injection, and initialization
     * ordering.
     * If you are in need of those more advanced features of an application context you would
     * be better off using your own application context and passing it via {@link #load(ConfigurableApplicationContext)}
     * @param applicationContextFile    the path to local application context file (inside the classpath)
     * @param defaultAction             the default action or {@code null} if none is meant
     * @return the executor
     * @throws Exception
     */
    public static Executor load(String applicationContextFile, String defaultAction) throws Exception {
        logger.info("Loading bean definitions from: " + applicationContextFile);
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final BeanDefinitionContainer container = new BeanDefinitionContainer();
        container.addAll(getBeansDefinitions(getContextContent(applicationContextFile)));
        container.addAll(getBeansDefinitions(getContextContent("/nemo/nemo.xml")));
        container.add(NEMO_EXECUTOR, Executor.class);
        stopWatch.stop();
        logger.info("Bootstrapping took " + stopWatch.getTotalTimeMillis() + "ms");
        final ConfigurableApplicationContext applicationContext = container.getApplicationContext();
        final Executor executor = applicationContext.getBean(Executor.class);
        executor.setDefaultActionName(defaultAction);
        return executor;
    }

    /**
     * This method is the same as {@link #load(org.springframework.context.ConfigurableApplicationContext)}
     * while not providing as much initial functionality as that, since this method only
     * accepts bean factories and not application context configurations.
     * This is meant to be used in harmony with {@link ExecutorBuilder}
     * @param parent    the parent bean factory containing actions
     * @return the configured executor instance.
     * @throws Exception
     * @see ExecutorBuilder
     */
    public static Executor load(BeanFactory parent) throws Exception {
        logger.info("Loading bean definitions from the provided bean factory");
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final BeanDefinitionContainer container = new BeanDefinitionContainer();
        container.addAll(getBeansDefinitions(getContextContent("/nemo/nemo.xml")));
        container.add(NEMO_EXECUTOR, Executor.class);
        stopWatch.stop();
        logger.info("Bootstrapping took " + stopWatch.getTotalTimeMillis() + "ms");
        final ConfigurableApplicationContext applicationContext = container.getApplicationContext();
        applicationContext.getBeanFactory().setParentBeanFactory(parent);
        final Executor executor = applicationContext.getBean(Executor.class);
        executor.setApplicationContext(applicationContext);
        return executor;
    }

}
