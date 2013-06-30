package com.agileapes.nemo.action.impl;

import com.agileapes.couteau.context.contract.OrderedBean;
import com.agileapes.couteau.context.error.FatalRegistryException;
import com.agileapes.couteau.context.error.RegistryException;
import com.agileapes.couteau.context.impl.AbstractThreadSafeContext;
import com.agileapes.couteau.context.impl.BeanProcessorAdapter;
import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.disassemble.DisassembleStrategy;
import com.agileapes.nemo.disassemble.impl.DisassembleStrategyContext;
import com.agileapes.nemo.error.ActionDefinitionException;
import com.agileapes.nemo.error.NoDefaultActionException;
import com.agileapes.nemo.error.NoStrategyAttributedException;
import com.agileapes.nemo.error.OptionDefinitionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The action registry is the entity in charge of holding actions in place. This registry acts as the central information
 * gathering entity which is the reference entity for all the actions in the system. If there is an action that can be
 * referenced from the execution context it must be registered here and vice versa.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 16:44)
 */
public class ActionContext extends AbstractThreadSafeContext<Object> {

    private final Set<Action> internalActions = new CopyOnWriteArraySet<Action>();
    private final Map<String, Action> actions = new ConcurrentHashMap<String, Action>();
    private final static Log log = LogFactory.getLog(ActionContext.class);
    private Action defaultAction = null;

    /**
     * This constructor will instantiate the registry, taking in the unique strategy context bean for the whole
     * application. This strategy context will later help determine which strategy should be used for disassembling
     * the actions registered with the system.
     * @param strategyContext    the strategy context
     */
    public ActionContext(final DisassembleStrategyContext strategyContext) {
        addBeanProcessor(new BeanProcessorAdapter<Object>(OrderedBean.HIGHEST_PRECEDENCE) {
            @Override
            public Object postProcessBeforeRegistration(Object bean, String beanName) throws RegistryException {
                log.info("Registering action <" + beanName + "> of type " + bean.getClass());
                final DisassembleStrategy<Object> strategy;
                try {
                    log.info("Attempting to discern action strategy for action " + beanName);
                    strategy = strategyContext.getStrategy(bean);
                } catch (NoStrategyAttributedException e) {
                    throw new FatalRegistryException("Could not find a strategy matching the requirements of action: " + beanName, e);
                }
                final SmartAction<Object> action;
                try {
                    action = new SmartAction<Object>(bean, strategy);
                } catch (OptionDefinitionException e) {
                    throw new ActionDefinitionException("Could not define action", e);
                }
                action.setName(beanName);
                actions.put(action.getName(), action);
                if (action.isDefaultAction()) {
                    if (action.isInternal()) {
                        throw new ActionDefinitionException("Actions cannot be both internal and marked as default: " + action.getName());
                    }
                    if (defaultAction != null) {
                        throw new ActionDefinitionException("Action <" + action.getName() + "> cannot be marked as default, because action <" + defaultAction.getName() + "> has already been set as the default action");
                    }
                    log.info("Discovered the default action: " + beanName);
                    defaultAction = action;
                } else if (action.isInternal()) {
                    log.info("Discovered an internal action: " + beanName);
                    internalActions.add(action);
                }
                return action;
            }
        });
        ready();
    }

    /**
     * This method will return the default action for the context, determined via {@link DisassembleStrategy#isDefaultAction(Object)}
     * or throw an exception
     * @return the default action
     * @throws NoDefaultActionException if no default action has been specified throughout the system
     */
    public Action getDefaultAction() throws NoDefaultActionException {
        if (defaultAction == null) {
            throw new NoDefaultActionException();
        }
        return defaultAction;
    }

    /**
     * @return will return a set of all the actions marked as internal
     */
    public Set<Action> getInternalActions() {
        return Collections.unmodifiableSet(internalActions);
    }

    /**
     * Will return a map of action target names to wrapped actions. This is handy when you want to access all the
     * actions.
     * <strong>NB</strong> Actions can be cast to {@link SmartAction} to access the strategy they offer
     * @return map of actions
     */
    public Map<String, Action> getActions() {
        return Collections.unmodifiableMap(actions);
    }

    /**
     * @return a set of all the names through which actions can be invoked. This set includes the internal actions,
     * which <em>cannot</em> be accessed externally.
     */
    public Set<String> getTargets() {
        return Collections.unmodifiableSet(actions.keySet());
    }

}
