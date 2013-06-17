package com.agileapes.nemo.error;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/17/13, 11:56 AM)
 */
public class EventTranslationException extends Exception {

    public EventTranslationException(String message) {
        super(message);
    }

    public EventTranslationException(String message, Throwable cause) {
        super(message, cause);
    }
}
