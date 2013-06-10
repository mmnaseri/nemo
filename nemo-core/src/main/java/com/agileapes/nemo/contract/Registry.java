package com.agileapes.nemo.contract;

import com.agileapes.nemo.error.RegistryException;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 16:42)
 */
public interface Registry<C> {

    void register(String name, C item) throws RegistryException;

    C get(String name) throws RegistryException;

    C[] find(Filter<C> filter) throws RegistryException;

}
