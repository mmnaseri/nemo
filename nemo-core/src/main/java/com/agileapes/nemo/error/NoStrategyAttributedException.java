package com.agileapes.nemo.error;

import com.agileapes.couteau.context.error.RegistryException;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/30, 9:41)
 */
public class NoStrategyAttributedException extends RegistryException {

    public NoStrategyAttributedException() {
        super("No strategy has been attributed to this action");
    }

}
