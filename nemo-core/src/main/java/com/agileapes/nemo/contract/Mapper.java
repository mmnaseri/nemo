package com.agileapes.nemo.contract;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 18:56)
 */
public interface Mapper<I, O> {

    O map(I item);

}
