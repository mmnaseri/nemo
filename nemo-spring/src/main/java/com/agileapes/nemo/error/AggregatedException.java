package com.agileapes.nemo.error;

import com.agileapes.nemo.util.ExceptionMessage;

import java.util.Arrays;
import java.util.List;

/**
 * This error class denotes a set of errors leading to a final decision that the normal process of
 * progressively working through the instructions should be stopped, with all the errors collected in one
 * place.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/17/13, 12:06 PM)
 */
public class AggregatedException extends Exception {

    private final List<Throwable> errors;

    private static String aggregate(List<Throwable> errors) {
        final StringBuilder builder = new StringBuilder();
        for (Throwable error : errors) {
            builder.append(new ExceptionMessage(error)).append("\n");
        }
        return builder.toString();
    }

    public AggregatedException(List<Throwable> errors) {
        super(aggregate(errors));
        this.errors = errors;
    }

    public AggregatedException(Throwable... errors) {
        this(Arrays.asList(errors));
    }

    public List<Throwable> getErrors() {
        return errors;
    }

}
