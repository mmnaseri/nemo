package com.agileapes.nemo.contract;

/**
 * This interface will allow its implementers to specify a priority based on which they can be accessed. This is specially
 * useful in specifying {@link BeanProcessor}s and having them ordered to be run in a certain way.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/12/13, 2:57 PM)
 */
public interface OrderedBean {

    /**
     * @return the (dynamic) ordering attributed with the implementing object
     */
    int getOrder();

}
