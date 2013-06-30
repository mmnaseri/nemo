package com.agileapes.nemo.exec;

import com.agileapes.couteau.context.error.RegistryException;
import com.agileapes.couteau.context.spring.ContextConfigurator;
import com.agileapes.couteau.context.spring.SpringContextConfigurator;
import com.agileapes.nemo.contract.ActionDiscoverer;
import com.agileapes.nemo.disassemble.DisassembleStrategy;
import com.agileapes.nemo.value.ValueReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This extension to {@link ExecutorContext} allows users to instantiate all their nemo-related beans through
 * Spring, without having to manually wire actions or anything else.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/12, 3:12)
 */
public class SpringExecutorContext extends ExecutorContext implements BeanFactoryPostProcessor {

    public static final String ACTION_PREFIX = "action:";
    private static final Log log = LogFactory.getLog(ExecutorContext.class.getCanonicalName().concat(".spring"));
    private final Set<ActionDiscoverer> discoverers = new HashSet<ActionDiscoverer>();

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        log.info("Starting to look through Spring's application context");
        log.info("Looking for ways to discover new actions: ");
        discoverers.addAll(beanFactory.getBeansOfType(ActionDiscoverer.class, false, true).values());
        refresh();
        final SpringContextConfigurator configurator = beanFactory.getBeansOfType(SpringContextConfigurator.class).values().iterator().next();
        try {
            configurator.configure(this, new ContextConfigurator<SpringExecutorContext>() {
                @Override
                public void configure(SpringExecutorContext context, ConfigurableListableBeanFactory beanFactory) {
                    handleStrategies(beanFactory);
                    handleValueReaders(beanFactory);
                    handleActions(beanFactory);
                }
            });
        } catch (RegistryException e) {
            throw new FatalBeanException("Failed to register context with Spring", e);
        }
    }

    private Set<Object> handleStrategies(ConfigurableListableBeanFactory beanFactory) {
        final Set<Object> newItems = new HashSet<Object>();
        final String[] names = beanFactory.getBeanNamesForType(DisassembleStrategy.class, false, true);
        log.info("Looking for strategies ...");
        for (String name : names) {
            try {
                final DisassembleStrategy strategy = beanFactory.getBean(name, DisassembleStrategy.class);
                newItems.add(strategy);
                log.debug("Discovered disassemble strategy: " + strategy.getClass().getCanonicalName());
                addDisassembleStrategy(strategy);
            } catch (RegistryException e) {
                throw new FatalBeanException("Failed to register strategy", e);
            }
        }
        return newItems;
    }

    private Set<Object> handleValueReaders(ConfigurableListableBeanFactory beanFactory) {
        final Set<Object> newItems = new HashSet<Object>();
        log.info("Looking for value readers ...");
        final String[] names = beanFactory.getBeanNamesForType(ValueReader.class, false, true);
        for (String name : names) {
            try {
                final ValueReader valueReader = beanFactory.getBean(name, ValueReader.class);
                newItems.add(valueReader);
                log.debug("Discovered value reader " + valueReader.getClass().getCanonicalName());
                addValueReader(valueReader);
            } catch (RegistryException e) {
                throw new FatalBeanException("Failed to register value reader", e);
            }
        }
        return newItems;
    }

    private Set<Object> handleActions(ConfigurableListableBeanFactory beanFactory) {
        final Set<Object> newItems = new HashSet<Object>();
        log.info("Looking for actions ...");
        for (ActionDiscoverer discoverer : discoverers) {
            log.info("Attempting to find new actions using " + discoverer.getClass().getCanonicalName());
            final Map<String, Object> map = discoverer.map(beanFactory);
            int found = newItems.size();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (newItems.add(entry.getValue())) {
                    try {
                        String name = entry.getKey();
                        if (name.startsWith(ACTION_PREFIX)) {
                            name = name.substring(ACTION_PREFIX.length());
                        }
                        addAction(name, entry.getValue());
                    } catch (RegistryException e) {
                        throw new FatalBeanException("Failed to register action", e);
                    }
                } else {
                    log.warn("Ignoring action <" + entry.getKey() + "> because it was already discovered by another discoverer");
                }
            }
            found = newItems.size() - found;
            log.info("Found " + found + " new action(s)");
        }
        return newItems;
    }

}
