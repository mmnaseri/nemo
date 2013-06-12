package com.agileapes.nemo.contract;

/**
 * This interface will help the process of replacing an item of a given type with another item (possibly from another
 * type hierarchy).
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 18:56)
 */
public interface Mapper<I, O> {

    /**
     * This method is expected to be able to map a given input object to the desired output object. <strong>Note</strong>
     * that {@code null} values might be acceptable in some contexts, both as the input and as the output
     * @param item    the input item
     * @return the mapped output item
     */
    O map(I item);

}
