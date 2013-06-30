package com.agileapes.nemo.error;

import com.agileapes.couteau.context.error.RegistryException;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/30, 9:35)
 */
public class ActionDefinitionException extends RegistryException {

    public ActionDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ActionDefinitionException(String message) {
        super(message);
    }

}
