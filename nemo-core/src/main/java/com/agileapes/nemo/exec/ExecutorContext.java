package com.agileapes.nemo.exec;

import com.agileapes.nemo.action.ActionRegistry;
import com.agileapes.nemo.contract.BeanProcessor;
import com.agileapes.nemo.contract.Callback;
import com.agileapes.nemo.contract.Filter;
import com.agileapes.nemo.contract.Registry;
import com.agileapes.nemo.contract.impl.AbstractThreadSafeRegistry;
import com.agileapes.nemo.disassemble.DisassembleStrategy;
import com.agileapes.nemo.disassemble.impl.DisassembleStrategyContext;
import com.agileapes.nemo.error.RegistryException;
import com.agileapes.nemo.value.ValueReader;
import com.agileapes.nemo.value.ValueReaderContext;
import com.agileapes.nemo.value.impl.DefaultValueReaderContext;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;

import static com.agileapes.nemo.util.CollectionDSL.with;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/11, 14:30)
 */
public class ExecutorContext extends AbstractThreadSafeRegistry<Object> {

    private final DisassembleStrategyContext strategyContext;
    private final ValueReaderContext valueReaderContext;
    private final ActionRegistry actionRegistry;
    private final Executor executor;

    public ExecutorContext() throws RegistryException {
        valueReaderContext = new DefaultValueReaderContext();
        strategyContext = new DisassembleStrategyContext(valueReaderContext);
        actionRegistry = new ActionRegistry(strategyContext);
        executor = new Executor(actionRegistry);
        registerBean(this, valueReaderContext);
        registerBean(this, strategyContext);
        registerBean(this, actionRegistry);
        registerBean(this, executor);
    }

    protected void addBean(String name, Object bean) throws RegistryException {
        if (name.equals(bean.getClass().getCanonicalName())) {
            name = name + "#" + getMap().size();
        }
        register(name, bean);
    }

    protected <B> void registerBean(Registry<B> registry, B bean) throws RegistryException {
        registerBean(registry, bean.getClass().getCanonicalName(), bean);
    }

    protected <B> void registerBean(Registry<B> registry, String name, B bean) throws RegistryException {
        addBean(name, bean);
        registry.register(name, bean);
    }

    public void addValueReader(ValueReader valueReader) throws RegistryException {
        registerBean(valueReaderContext, valueReader);
    }

    public void addDisassembleStrategy(DisassembleStrategy strategy) throws RegistryException {
        registerBean(strategyContext, strategy);
    }

    public void addAction(String target, Object action) throws RegistryException {
        registerBean(actionRegistry, target, action);
    }

    public void addBeanProcessor(BeanProcessor beanProcessor) throws RegistryException {
        addBean(beanProcessor.getClass().getCanonicalName(), beanProcessor);
    }

    public DisassembleStrategyContext getStrategyContext() {
        return strategyContext;
    }

    public ValueReaderContext getValueReaderContext() {
        return valueReaderContext;
    }

    public ActionRegistry getActionRegistry() {
        return actionRegistry;
    }

    private void runPostProcessors() throws RegistryException {
        with(Arrays.asList(find(new Filter<Object>() {
            @Override
            public boolean accepts(Object item) {
                return item instanceof BeanProcessor;
            }
        }))).each(new Callback<Object>() {
            @Override
            public void perform(Object obj) {
                final BeanProcessor processor = (BeanProcessor) obj;
                with(getMap().entrySet())
                        .each(new Callback<Map.Entry<String, Object>>() {
                            @Override
                            public void perform(Map.Entry<String, Object> item) {
                                try {
                                    processor.processBean(item.getKey(), item.getValue());
                                } catch (Exception e) {
                                    throw new RuntimeException("Error occurred while processing " + item.getKey(), e);
                                }
                            }
                        });
            }
        });
    }

    public void execute(PrintStream out, String... args) throws Exception {
        runPostProcessors();
        executor.execute(out, args);
    }

    public void execute(String... args) throws Exception {
        runPostProcessors();
        executor.execute(args);
    }

}
