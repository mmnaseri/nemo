package com.agileapes.nemo.error;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 17:54)
 */
public class InvalidStrategyNameException extends RegistryException {

    public InvalidStrategyNameException(String actual, String expected) {
        super("Expected strategy name to be <" + expected + "> while it was <" + actual + ">");
    }

}
