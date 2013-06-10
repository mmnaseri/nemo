package com.agileapes.nemo.error;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 17:54)
 */
public class InvalidItemNameException extends RegistryException {

    public InvalidItemNameException(String actual, String expected) {
        super("Expected item name to be <" + expected + "> while it was <" + actual + ">");
    }

}
