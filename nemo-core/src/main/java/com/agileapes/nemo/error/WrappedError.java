package com.agileapes.nemo.error;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/12, 2:58)
 */
public class WrappedError extends Error {

    private final Throwable wrappedError;

    public WrappedError(Throwable wrappedError) {
        super(wrappedError);
        this.wrappedError = wrappedError;
    }

    public <T extends Throwable> T getWrappedError(Class<T> type) {
        //noinspection unchecked
        return (T) wrappedError;
    }

}
