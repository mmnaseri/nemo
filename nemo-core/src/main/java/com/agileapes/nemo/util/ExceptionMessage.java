package com.agileapes.nemo.util;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/13/13, 12:08 PM)
 */
public class ExceptionMessage {

    private final Throwable throwable;

    public ExceptionMessage(Throwable throwable) {
        this.throwable = throwable;
    }

    public String getMessage() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(throwable.getMessage() == null ? throwable.getClass().getName() : throwable.getMessage());
        if (throwable.getCause() != null) {
            stringBuilder.append("\nCaused by ");
            stringBuilder.append(new ExceptionMessage(throwable.getCause()).getMessage());
        }
        return stringBuilder.toString();
    }

}
