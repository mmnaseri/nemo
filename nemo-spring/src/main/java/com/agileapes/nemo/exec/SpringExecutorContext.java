package com.agileapes.nemo.exec;

import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.api.Disassembler;
import com.agileapes.nemo.contract.BeanProcessor;
import com.agileapes.nemo.contract.impl.SpringBeanProcessor;
import com.agileapes.nemo.disassemble.DisassembleStrategy;
import com.agileapes.nemo.error.RegistryException;
import com.agileapes.nemo.value.ValueReader;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/12, 3:12)
 */
public class SpringExecutorContext extends ExecutorContext implements BeanFactoryPostProcessor {

    public static final String ACTION_PREFIX = "action:";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        final Set<Object> springContextItems = new HashSet<Object>();
        springContextItems.addAll(handleStrategies(beanFactory));
        springContextItems.addAll(handleValueReaders(beanFactory));
        springContextItems.addAll(handleActions(beanFactory));
        for (Map.Entry<String, Object> entry : getMap().entrySet()) {
            if (springContextItems.contains(entry.getValue())) {
                continue;
            }
            beanFactory.registerSingleton(entry.getKey(), entry.getValue());
        }
        final List<BeanProcessor> processors = getBeanProcessors();
        final Set<BeanPostProcessor> newProcessors = new HashSet<BeanPostProcessor>();
        for (BeanProcessor processor : processors) {
            final SpringBeanProcessor beanPostProcessor = new SpringBeanProcessor(processor);
            newProcessors.add(beanPostProcessor);
            beanFactory.addBeanPostProcessor(beanPostProcessor);
        }
        final Map<String, BeanPostProcessor> postProcessorMap = beanFactory.getBeansOfType(BeanPostProcessor.class, false, true);
        for (BeanPostProcessor processor : postProcessorMap.values()) {
            if (newProcessors.contains(processor)) {
                continue;
            }
            for (Map.Entry<String, Object> entry : getMap().entrySet()) {
                if (springContextItems.contains(entry.getValue())) {
                    continue;
                }
                Object first = processor.postProcessBeforeInitialization(entry.getValue(), entry.getKey());
                if (first == null) {
                    first = entry.getValue();
                }
                Object second = processor.postProcessAfterInitialization(first, entry.getKey());
                if (second == null) {
                    second = first;
                }
                getMap().put(entry.getKey(), second);
            }
        }
    }

    private Set<Object> handleStrategies(ConfigurableListableBeanFactory beanFactory) {
        final Set<Object> newItems = new HashSet<Object>();
        final String[] names = beanFactory.getBeanNamesForType(DisassembleStrategy.class, false, true);
        for (String name : names) {
            try {
                final DisassembleStrategy strategy = beanFactory.getBean(name, DisassembleStrategy.class);
                newItems.add(strategy);
                addDisassembleStrategy(strategy);
            } catch (RegistryException e) {
                throw new FatalBeanException("Failed to register strategy", e);
            }
        }
        return newItems;
    }

    private Set<Object> handleValueReaders(ConfigurableListableBeanFactory beanFactory) {
        final Set<Object> newItems = new HashSet<Object>();
        final String[] names = beanFactory.getBeanNamesForType(ValueReader.class, false, true);
        for (String name : names) {
            try {
                final ValueReader valueReader = beanFactory.getBean(name, ValueReader.class);
                newItems.add(valueReader);
                addValueReader(valueReader);
            } catch (RegistryException e) {
                throw new FatalBeanException("Failed to register value reader", e);
            }
        }
        return newItems;
    }

    private Set<Object> handleActions(ConfigurableListableBeanFactory beanFactory) {
        final Set<Object> newItems = new HashSet<Object>();
        final String[] names = beanFactory.getBeanNamesForType(Action.class, false, true);
        for (String name : names) {
            try {
                final Action action = beanFactory.getBean(name, Action.class);
                newItems.add(action);
                if (name.startsWith(ACTION_PREFIX)) {
                    name = name.substring(ACTION_PREFIX.length());
                }
                addAction(name, action);
            } catch (RegistryException e) {
                throw new FatalBeanException("Failed to register value reader", e);
            }
        }
        final Map<String, Object> beans = beanFactory.getBeansWithAnnotation(Disassembler.class);
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            if (newItems.contains(entry.getValue())) {
                continue;
            }
            newItems.add(entry.getValue());
            try {
                addAction(entry.getKey(), entry.getValue());
            } catch (RegistryException e) {
                throw new FatalBeanException("Failed to register value reader", e);
            }
        }
        return newItems;
    }

}
