package com.agileapes.nemo.contract;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 18:54)
 */
public interface Callback<I> {

    void perform(I item);

}
