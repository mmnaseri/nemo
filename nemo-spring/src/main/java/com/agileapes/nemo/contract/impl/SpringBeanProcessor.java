package com.agileapes.nemo.contract.impl;

import com.agileapes.nemo.contract.BeanProcessor;
import com.agileapes.nemo.error.RegistryException;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/12/13, 4:52 PM)
 */
public class SpringBeanProcessor implements BeanPostProcessor {

    private final BeanProcessor beanProcessor;

    public SpringBeanProcessor(BeanProcessor beanProcessor) {
        this.beanProcessor = beanProcessor;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        try {
            return beanProcessor.postProcessBeforeRegistration(bean, beanName);
        } catch (RegistryException e) {
            throw new FatalBeanException("Failed to process bean: " + beanName, e);
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        try {
            return beanProcessor.postProcessBeforeDispense(bean, beanName);
        } catch (RegistryException e) {
            throw new FatalBeanException("Failed to process bean: " + beanName, e);
        }
    }

}
