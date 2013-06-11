package com.agileapes.nemo.contract;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/11, 14:41)
 */
public interface BeanProcessor {

    Object processBean(String name, Object bean) throws Exception;

}
