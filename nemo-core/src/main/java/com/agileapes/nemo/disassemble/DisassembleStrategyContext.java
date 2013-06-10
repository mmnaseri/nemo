package com.agileapes.nemo.disassemble;

import com.agileapes.nemo.api.Disassembler;
import com.agileapes.nemo.contract.Filter;
import com.agileapes.nemo.contract.impl.AbstractThreadSafeRegistry;
import com.agileapes.nemo.disassemble.impl.AnnotatedFieldsDisassembleStrategy;
import com.agileapes.nemo.error.ActionRefusedByStrategyException;
import com.agileapes.nemo.error.NoStrategyAttributedException;
import com.agileapes.nemo.error.NoSuchItemException;
import com.agileapes.nemo.error.RegistryException;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 17:17)
 */
public class DisassembleStrategyContext extends AbstractThreadSafeRegistry<DisassembleStrategy> {

    public static final Class<AnnotatedFieldsDisassembleStrategy> DEFAULT_STRATEGY = AnnotatedFieldsDisassembleStrategy.class;

    public DisassembleStrategyContext() {
        namesAreTypeSpecific = true;
    }

    @Override
    protected DisassembleStrategy postProcessBeforeRegister(String name, DisassembleStrategy item) throws RegistryException {
        return item;
    }

    @SuppressWarnings("unchecked")
    public DisassembleStrategy<Object> getStrategy(final Object action) throws RegistryException {
        if (action instanceof DisassemblerAware) {
            return ((DisassemblerAware) action).getDisassembler();
        }
        final Class<? extends DisassembleStrategy> strategy;
        boolean specified = false;
        if (action.getClass().isAnnotationPresent(Disassembler.class)) {
            specified = true;
            strategy = action.getClass().getAnnotation(Disassembler.class).value();
        } else {
            strategy = DEFAULT_STRATEGY;
        }
        DisassembleStrategy disassembleStrategy;
        try {
            disassembleStrategy = get(strategy.getCanonicalName());
        } catch (NoSuchItemException e) {
            throw new NoStrategyAttributedException();
        }
        if (disassembleStrategy.accepts(action)) {
            return disassembleStrategy;
        } else {
            if (specified) {
                throw new ActionRefusedByStrategyException(strategy);
            } else {
                final DisassembleStrategy[] candidates = find(new Filter<DisassembleStrategy>() {
                    @Override
                    public boolean accepts(DisassembleStrategy item) {
                        return item.accepts(action);
                    }
                });
                if (candidates.length == 0) {
                    throw new NoStrategyAttributedException();
                } else {
                    return candidates[0];
                }
            }
        }
    }

}
