package com.agileapes.nemo.action;

import com.agileapes.nemo.contract.impl.AbstractThreadSafeRegistry;
import com.agileapes.nemo.disassemble.DisassembleStrategyContext;
import com.agileapes.nemo.error.FatalRegistryException;
import com.agileapes.nemo.error.NoDefaultActionException;
import com.agileapes.nemo.error.NoStrategyAttributedException;
import com.agileapes.nemo.error.RegistryException;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 16:44)
 */
public class ActionRegistry extends AbstractThreadSafeRegistry<Object> {

    private final DisassembleStrategyContext strategyContext;
    private final Set<Action> internalActions = new CopyOnWriteArraySet<Action>();
    private final Map<String, Action> actions = new ConcurrentHashMap<String, Action>();
    private Action defaultAction = null;

    public ActionRegistry(DisassembleStrategyContext strategyContext) {
        this.strategyContext = strategyContext;
    }

    @Override
    protected synchronized Object postProcessBeforeRegister(String name, Object item) throws RegistryException {
        try {
            final SmartAction<Object> action = new SmartAction<Object>(item, strategyContext.getStrategy(item));
            action.setName(name);
            actions.put(action.getName(), action);
            if (action.isDefaultAction()) {
                if (action.isInternal()) {
                    throw new IllegalStateException("Actions cannot be both internal and marked as default: " + action.getName());
                }
                if (defaultAction != null) {
                    throw new IllegalStateException("Action " + action.getName() + " cannot be marked as default, because action " + defaultAction.getName() + " has already been set as the default action");
                }
                defaultAction = action;
            } else if (action.isInternal()) {
                internalActions.add(action);
            }
            return action;
        } catch (NoStrategyAttributedException e) {
            throw new FatalRegistryException("Could not find a strategy matching the requirements of action: " + name, e);
        }
    }

    public Action getDefaultAction() throws NoDefaultActionException {
        if (defaultAction == null) {
            throw new NoDefaultActionException();
        }
        return defaultAction;
    }

    public Set<Action> getInternalActions() {
        return Collections.unmodifiableSet(internalActions);
    }

    public Map<String, Action> getActions() {
        return Collections.unmodifiableMap(actions);
    }

    public Set<String> getTargets() {
        return Collections.unmodifiableSet(actions.keySet());
    }

}
