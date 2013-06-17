package com.agileapes.nemo.exec;

import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.api.Command;
import com.agileapes.nemo.api.Disassembler;
import com.agileapes.nemo.contract.ActionDiscoverer;
import com.agileapes.nemo.contract.BeanProcessor;
import com.agileapes.nemo.contract.impl.AnnotatedActionDiscoverer;
import com.agileapes.nemo.contract.impl.SpringBeanProcessor;
import com.agileapes.nemo.contract.impl.TypedActionDiscoverer;
import com.agileapes.nemo.disassemble.DisassembleStrategy;
import com.agileapes.nemo.error.RegistryException;
import com.agileapes.nemo.events.SpringEventTranslator;
import com.agileapes.nemo.events.TranslationScheme;
import com.agileapes.nemo.events.impl.BuiltInTranslationScheme;
import com.agileapes.nemo.events.impl.GenericTranslationScheme;
import com.agileapes.nemo.value.ValueReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/12, 3:12)
 */
public class SpringExecutorContext extends ExecutorContext implements BeanFactoryPostProcessor, ApplicationContextAware {

    public static final String ACTION_PREFIX = "action:";
    private static final Log log = LogFactory.getLog(ExecutorContext.class.getCanonicalName().concat(".spring"));
    private final Set<ActionDiscoverer> discoverers;
    private final SpringEventTranslator translator;

    public SpringExecutorContext() throws RegistryException {
        discoverers = new HashSet<ActionDiscoverer>();
        discoverers.add(new TypedActionDiscoverer(Action.class));
        discoverers.add(new AnnotatedActionDiscoverer(Disassembler.class));
        discoverers.add(new AnnotatedActionDiscoverer(Command.class));
        translator = new SpringEventTranslator();
        translator.register("builtInTranslator", new BuiltInTranslationScheme());
        translator.register("genericTranslator", new GenericTranslationScheme());
        addEventListener(translator);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        log.info("Starting to look through Spring's application context");
        log.info("Looking for ways to discover new actions: ");
        discoverers.addAll(beanFactory.getBeansOfType(ActionDiscoverer.class, false, true).values());
        final Set<Object> springContextItems = new HashSet<Object>();
        handleEventTranslationSchemes(beanFactory);
        springContextItems.addAll(handleStrategies(beanFactory));
        springContextItems.addAll(handleValueReaders(beanFactory));
        springContextItems.addAll(handleActions(beanFactory));
        log.info("Registering internal beans with Spring's application context");
        for (Map.Entry<String, Object> entry : getMap().entrySet()) {
            if (springContextItems.contains(entry.getValue())) {
                continue;
            }
            log.debug("Adding bean " + entry.getKey());
            beanFactory.registerSingleton(entry.getKey(), entry.getValue());
        }
        log.info("Registering internal bean post processors through the application context ...");
        final List<BeanProcessor> processors = getBeanProcessors();
        final Set<BeanPostProcessor> newProcessors = new HashSet<BeanPostProcessor>();
        for (BeanProcessor processor : processors) {
            final SpringBeanProcessor beanPostProcessor = new SpringBeanProcessor(processor);
            newProcessors.add(beanPostProcessor);
            beanFactory.addBeanPostProcessor(beanPostProcessor);
        }
        log.info("Applying Spring's bean post processors on the ExecutorContext ...");
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

    private void handleEventTranslationSchemes(ConfigurableListableBeanFactory beanFactory) {
        final Map<String, TranslationScheme> beansOfType = beanFactory.getBeansOfType(TranslationScheme.class, false, true);
        for (Map.Entry<String, TranslationScheme> entry : beansOfType.entrySet()) {
            try {
                translator.register(entry.getKey(), entry.getValue());
            } catch (RegistryException e) {
                throw new FatalBeanException("Failed to register translation scheme", e);
            }
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        translator.setApplicationContext(applicationContext);
    }

}
