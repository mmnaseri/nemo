package com.agileapes.nemo.error;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/12, 2:48)
 */
public class FatalExecutionException extends Exception {

    public FatalExecutionException(String message) {
        super(message);
    }

    public FatalExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

}
