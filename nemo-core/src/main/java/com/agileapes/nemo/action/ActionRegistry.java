package com.agileapes.nemo.action;

import com.agileapes.nemo.contract.impl.AbstractThreadSafeRegistry;
import com.agileapes.nemo.disassemble.DisassembleStrategyContext;
import com.agileapes.nemo.error.FatalRegistryException;
import com.agileapes.nemo.error.NoStrategyAttributedException;
import com.agileapes.nemo.error.RegistryException;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 16:44)
 */
public class ActionRegistry extends AbstractThreadSafeRegistry<Object> {

    private final DisassembleStrategyContext strategyContext;

    public ActionRegistry(DisassembleStrategyContext strategyContext) {
        this.strategyContext = strategyContext;
    }

    @Override
    protected Object postProcessBeforeRegister(String name, Object item) throws RegistryException {
        try {
            return new SmartAction<Object>(item, strategyContext.getStrategy(item));
        } catch (NoStrategyAttributedException e) {
            throw new FatalRegistryException("Could not find a strategy matching the requirements of action: " + name, e);
        }
    }

}
