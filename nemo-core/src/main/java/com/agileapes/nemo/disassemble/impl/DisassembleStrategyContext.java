package com.agileapes.nemo.disassemble.impl;

import com.agileapes.nemo.api.Disassembler;
import com.agileapes.nemo.contract.Filter;
import com.agileapes.nemo.contract.Mapper;
import com.agileapes.nemo.contract.impl.AbstractThreadSafeContext;
import com.agileapes.nemo.disassemble.DisassembleStrategy;
import com.agileapes.nemo.disassemble.DisassemblerAware;
import com.agileapes.nemo.error.ActionRefusedByStrategyException;
import com.agileapes.nemo.error.NoStrategyAttributedException;
import com.agileapes.nemo.error.NoSuchItemException;
import com.agileapes.nemo.error.RegistryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;

import static com.agileapes.nemo.util.CollectionDSL.with;

/**
 * This is a context aware of all the strategies throughout the application. All strategies not registered with this context
 * will be ignored by the executor.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 17:17)
 */
public class DisassembleStrategyContext extends AbstractThreadSafeContext<DisassembleStrategy> {

    public static final Class<AnnotatedFieldsDisassembleStrategy> DEFAULT_STRATEGY = AnnotatedFieldsDisassembleStrategy.class;
    private static final Log log = LogFactory.getLog(DisassembleStrategyContext.class);

    public DisassembleStrategyContext() {
        namesAreTypeSpecific = true;
    }

    @SuppressWarnings("unchecked")
    public DisassembleStrategy<Object> getStrategy(final Object action) throws RegistryException {
        log.info("Finding disassembler for action: " + action);
        if (action instanceof DisassemblerAware) {
            log.info("Action is aware of its disassembler");
            final DisassembleStrategy disassembler = ((DisassemblerAware) action).getDisassembler();
            if (!disassembler.accepts(action)) {
                log.error("Disassembler does not accept the action");
                throw new ActionRefusedByStrategyException(disassembler.getClass());
            }
            return disassembler;
        }
        final Class<? extends DisassembleStrategy> strategy;
        boolean specified = false;
        if (action.getClass().isAnnotationPresent(Disassembler.class)) {
            specified = true;
            log.info("Disassembler specified for action via @Disassembler");
            strategy = action.getClass().getAnnotation(Disassembler.class).value();
        } else {
            log.info("No disassembler specified for action. Falling back to the default disassembler strategy " + DEFAULT_STRATEGY.getCanonicalName());
            strategy = DEFAULT_STRATEGY;
        }
        DisassembleStrategy disassembleStrategy;
        try {
            disassembleStrategy = get(strategy.getCanonicalName());
        } catch (NoSuchItemException e) {
            log.error("Specified strategy " + strategy.getCanonicalName() + " has not been registered with the context");
            throw new NoStrategyAttributedException();
        }
        if (disassembleStrategy.accepts(action)) {
            return disassembleStrategy;
        } else {
            if (specified) {
                log.error("Disassembler does not accept the action");
                throw new ActionRefusedByStrategyException(strategy);
            } else {
                log.error("Determined strategy does not accept action. Attempting to find candidates.");
                final DisassembleStrategy[] candidates = find(new Filter<DisassembleStrategy>() {
                    @Override
                    public boolean accepts(DisassembleStrategy item) {
                        return item.accepts(action);
                    }
                });
                log.info("Found " + candidates.length + " candidates as disassembler strategy for action " + action);
                if (candidates.length == 0) {
                    throw new NoStrategyAttributedException();
                } else {
                    log.debug("Candidates are " + Arrays.toString(with(candidates).map(new Mapper<DisassembleStrategy, String>() {
                        @Override
                        public String map(DisassembleStrategy item) {
                            return item.getClass().getCanonicalName();
                        }
                    }).array()));
                    log.info("Chose " + candidates[0].getClass().getCanonicalName() + " for action " + action);
                    return candidates[0];
                }
            }
        }
    }

    @Override
    protected Class<DisassembleStrategy> getType() {
        return DisassembleStrategy.class;
    }

}
