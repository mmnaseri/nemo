package com.agileapes.nemo.error;

import com.agileapes.couteau.context.error.RegistryException;
import com.agileapes.nemo.disassemble.DisassembleStrategy;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/30, 9:36)
 */
public class ActionRefusedByStrategyException extends RegistryException {

    public ActionRefusedByStrategyException(Class<? extends DisassembleStrategy> strategyClass) {
        super("Action has been refused by strategy: " + strategyClass.getCanonicalName());
    }

}
