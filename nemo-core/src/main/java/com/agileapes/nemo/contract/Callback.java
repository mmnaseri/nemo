package com.agileapes.nemo.contract;

/**
 * This is an interface encapsulating the unit of work expected in many contexts and is run whenever a single action
 * without returning a value is expected to be performed on a given object
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 18:54)
 */
public interface Callback<I> {

    /**
     * This method will be called by the context to indicate that the given object is now available and can be called upon
     * from inside the callback
     * @param item    the item
     */
    void perform(I item);

}
