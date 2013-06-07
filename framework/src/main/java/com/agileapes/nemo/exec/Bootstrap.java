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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.util.ClassUtils;
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
public class Bootstrap {

    public static final String NEMO_EXECUTOR = "nemoExecutor";
    public static final String NEMO_EXECUTION_DEFAULT = "/nemo/execution.xml";
    private static final Logger logger = Logger.getLogger(Bootstrap.class);

    /**
     * This method loads the application context file for nemo from the
     * classpath and returns its content
     * @return the content of the application context file
     * @throws IOException
     */
    private static String getContextContent() throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(Bootstrap.class.getResourceAsStream("/nemo/nemo.xml")));
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
     * @return a map of baens
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static Map<String, Class<?>> getBeansDefinitions() throws IOException, ClassNotFoundException {
        final String content = getContextContent();
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
        logger.info("Building nemo backbone context configuration");
        final StaticApplicationContext context = new StaticApplicationContext();
        applicationContext.setParent(context);
        final Map<String, Class<?>> map = getBeansDefinitions();
        map.put(NEMO_EXECUTOR, Executor.class);
        for (Map.Entry<String, Class<?>> entry : map.entrySet()) {
            logger.debug("Adding singleton bean of type <" + entry.getValue().getCanonicalName() + "> with unique identifier: " + entry.getKey());
            context.registerSingleton(entry.getKey(), entry.getValue());
        }
        context.refresh();
        for (final Class<?> type : map.values()) {
            if (BeanPostProcessor.class.isAssignableFrom(type)) {
                logger.debug("Registering bean post processor of type " + type.getCanonicalName());
                applicationContext.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {
                    @Override
                    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                        beanFactory.addBeanPostProcessor((BeanPostProcessor) context.getBean(type));
                    }
                });
            }
            if (BeanFactoryPostProcessor.class.isAssignableFrom(type)) {
                logger.debug("Registering bean factory post processor of type " + type.getCanonicalName());
                applicationContext.addBeanFactoryPostProcessor((BeanFactoryPostProcessor) context.getBean(type));
            }
        }
        applicationContext.refresh();
        final ConfigurableApplicationContext bootstrappedContext = new Multicaster(applicationContext).publishEvent(new NemoBootstrappedEvent(applicationContext)).getApplicationContext();
        logger.info("Context config file bootstrapped, dispensing executor");
        final Executor executor = bootstrappedContext.getBean(Executor.class);
        executor.setApplicationContext(bootstrappedContext);
        return executor;
    }

    /**
     * This is the same as {@link #load(org.springframework.context.ConfigurableApplicationContext)} but it instantiates
     * its own application context, assuming that the configuration for the application is available under {@code /nemo/execution.xml},
     * otherwise it will raise a {@link FileNotFoundException}.
     * @return the executor instantiated by the bootstrapping mechanism
     * @throws Exception
     * @see #load(org.springframework.context.ConfigurableApplicationContext)
     */
    public static Executor load() throws Exception {
        logger.info("Loading execution context from predesignated file :" + NEMO_EXECUTION_DEFAULT);
        final URL resource = Bootstrap.class.getResource(NEMO_EXECUTION_DEFAULT);
        if (resource == null) {
            throw new FileNotFoundException("File " + NEMO_EXECUTION_DEFAULT + " not found under the running module.");
        }
        return load(new FileSystemXmlApplicationContext(resource.toExternalForm()));
    }

}
