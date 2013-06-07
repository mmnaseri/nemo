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

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/7, 14:12)
 */
class BeanDefinitionContainer {

    private final Map<String, Class<?>> beans = new HashMap<String, Class<?>>();

    BeanDefinitionContainer add(String name, Class<?> type) {
        beans.put(name, type);
        return this;
    }

    BeanDefinitionContainer addAll(Map<String, Class<?>> map) {
        for (Map.Entry<String, Class<?>> entry : map.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
        return this;
    }

    ConfigurableApplicationContext getApplicationContext() {
        final StaticApplicationContext applicationContext = new StaticApplicationContext();
        for (Map.Entry<String, Class<?>> entry : beans.entrySet()) {
            applicationContext.registerSingleton(entry.getKey(), entry.getValue());
        }
        for (Class<?> type : beans.values()) {
            if (BeanFactoryPostProcessor.class.isAssignableFrom(type)) {
                ((BeanFactoryPostProcessor) applicationContext.getBean(type)).postProcessBeanFactory(applicationContext.getBeanFactory());
            }
        }
        applicationContext.refresh();
        return applicationContext;
    }

}

