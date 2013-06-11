package com.agileapes.nemo.exec;

import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.api.Disassembler;
import com.agileapes.nemo.contract.BeanProcessor;
import com.agileapes.nemo.disassemble.DisassembleStrategy;
import com.agileapes.nemo.error.RegistryException;
import com.agileapes.nemo.value.ValueReader;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/12, 3:12)
 */
public class SpringExecutorContext extends ExecutorContext implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        final Set<Object> springContextItems = new HashSet<Object>();
        final Set<Object> postProcessors = new HashSet<Object>();
        handleStrategies(beanFactory, springContextItems);
        handleValueReaders(beanFactory, springContextItems);
        handleActions(beanFactory, springContextItems);
        for (Map.Entry<String, Object> entry : getMap().entrySet()) {
            if (springContextItems.contains(entry.getValue())) {
                continue;
            }
            beanFactory.registerSingleton(entry.getKey(), entry.getValue());
            if (entry.getValue() instanceof BeanProcessor) {
                final BeanProcessor processor = (BeanProcessor) entry.getValue();
                final BeanPostProcessor beanPostProcessor = new BeanPostProcessor() {
                    @Override
                    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                        try {
                            processor.processBean(beanName, bean);
                        } catch (Exception e) {
                            throw new FatalBeanException("Failed to process bean: " + beanName, e);
                        }
                        return bean;
                    }

                    @Override
                    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                        return bean;
                    }
                };
                postProcessors.add(beanPostProcessor);
                beanFactory.addBeanPostProcessor(beanPostProcessor);
            }
        }
        final Map<String, BeanPostProcessor> postProcessorMap = beanFactory.getBeansOfType(BeanPostProcessor.class);
        for (BeanPostProcessor processor : postProcessorMap.values()) {
            if (postProcessors.contains(processor)) {
                continue;
            }
            for (Map.Entry<String, Object> entry : getMap().entrySet()) {
                Object object = processor.postProcessBeforeInitialization(entry.getValue(), entry.getKey());
                object = processor.postProcessAfterInitialization(entry.getValue(), entry.getKey());
                getMap().put(entry.getKey(), object);
            }
        }

    }

    private void handleStrategies(ConfigurableListableBeanFactory beanFactory, Set<Object> newItems) {
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
    }

    private void handleValueReaders(ConfigurableListableBeanFactory beanFactory, Set<Object> newItems) {
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
    }

    private void handleActions(ConfigurableListableBeanFactory beanFactory, Set<Object> newItems) {
        final String[] names = beanFactory.getBeanNamesForType(Action.class, false, true);
        for (String name : names) {
            try {
                final Action action = beanFactory.getBean(name, Action.class);
                newItems.add(action);
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
    }

}
