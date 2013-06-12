package com.agileapes.nemo.contract;

/**
 * The context interface is an extension to the registry interface which avails the outside world of the benefits
 * of having post processors for registered beans.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/12/13, 2:55 PM)
 */
public interface Context<C> extends Registry<C> {

    /**
     * This method will add a new bean processor to the context. Note that bean processors themselves are not
     * a part of the context.
     * @param beanProcessor    the processor to be added
     */
    void addBeanProcessor(BeanProcessor beanProcessor);

}
