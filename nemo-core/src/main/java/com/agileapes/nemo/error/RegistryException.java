package com.agileapes.nemo.error;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 17:46)
 */
public abstract class RegistryException extends Exception {

    protected RegistryException() {
    }

    protected RegistryException(String message) {
        super(message);
    }

    protected RegistryException(String message, Throwable cause) {
        super(message, cause);
    }

    protected RegistryException(Throwable cause) {
        super(cause);
    }
}
