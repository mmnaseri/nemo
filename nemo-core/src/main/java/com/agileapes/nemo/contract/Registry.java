package com.agileapes.nemo.contract;

import com.agileapes.nemo.error.RegistryException;

/**
 * The registry will act as a central entity "in charge" of containing uniquely named beans of a certain type.
 * Generally, registries should be used for a specific type of objects, and using them as generic contexts for
 * all descendants of {@link Object} will depreciate their value considerably.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 16:42)
 */
public interface Registry<C> {

    /**
     * This method should be called whenever a new bean is to be registered with the registry. Take note that the
     * name should be unique across the context.
     * @param name    the name of the object
     * @param item    the object to be added
     * @throws RegistryException
     */
    void register(String name, C item) throws RegistryException;

    /**
     * This method will give access to the beans defined and registered with the context.
     * @param name    the unique name to the bean
     * @return the bean with the specified name
     * @throws RegistryException
     */
    C get(String name) throws RegistryException;

    /**
     * This method will return an array of all items accepted by the given filters
     * @param filter    the filter
     * @return the array of objects accepted by the filter
     * @throws RegistryException
     */
    C[] find(Filter<C> filter) throws RegistryException;

}
