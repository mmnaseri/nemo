package com.agileapes.nemo.contract;

import com.agileapes.nemo.error.RegistryException;

/**
 * The bean processor enables the contract to enable processing of beans available to any given context.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/11, 14:41)
 */
public interface BeanProcessor {

    /**
     * This method is called at the moment when the bean is being registered with the context
     * @param bean        the bean
     * @param beanName    the name of the bean as registered with the context
     * @return the bean or a wrapped version of the bean
     * @throws RegistryException
     */
    Object postProcessBeforeRegistration(Object bean, String beanName) throws RegistryException;

    /**
     * This method is called whenever a bean is being accessed from the context to be dispensed from
     * the context to the outside
     * @param bean        the bean
     * @param beanName    the name of the bean as registered with the context
     * @return the bean or a wrapped version of the bean
     * @throws RegistryException
     */
    Object postProcessBeforeDispense(Object bean, String beanName) throws RegistryException;

}
