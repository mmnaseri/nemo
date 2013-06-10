package com.agileapes.nemo.error;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 16:43)
 */
public class NoSuchItemException extends RegistryException {

    public NoSuchItemException(String name) {
        super("No such item: " + name);
    }
}
