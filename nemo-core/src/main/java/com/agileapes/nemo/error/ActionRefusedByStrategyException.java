package com.agileapes.nemo.error;

import com.agileapes.nemo.disassemble.DisassembleStrategy;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 18:01)
 */
public class ActionRefusedByStrategyException extends RegistryException {

    public ActionRefusedByStrategyException(Class<? extends DisassembleStrategy> strategy) {
        super("Strategy has refused action: " + strategy.getCanonicalName());
    }

}
