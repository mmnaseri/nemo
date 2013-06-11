package com.agileapes.nemo.error;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/11, 15:05)
 */
public class TargetNotFoundException extends Exception {

    public TargetNotFoundException(String target) {
        super("Unknown execution target: " + target);
    }
}
