package com.agileapes.nemo.exec;

import com.agileapes.nemo.action.impl.ActionContext;
import com.agileapes.nemo.contract.BeanProcessor;
import com.agileapes.nemo.contract.Registry;
import com.agileapes.nemo.contract.impl.AbstractBeanProcessor;
import com.agileapes.nemo.contract.impl.AbstractThreadSafeContext;
import com.agileapes.nemo.disassemble.DisassembleStrategy;
import com.agileapes.nemo.disassemble.impl.AnnotatedFieldsDisassembleStrategy;
import com.agileapes.nemo.disassemble.impl.DisassembleStrategyContext;
import com.agileapes.nemo.error.RegistryException;
import com.agileapes.nemo.value.ValueReader;
import com.agileapes.nemo.value.ValueReaderAware;
import com.agileapes.nemo.value.ValueReaderContext;
import com.agileapes.nemo.value.impl.*;

import java.io.PrintStream;

/**
 * The executor context is the central context which holds all the other pieces of information required for the
 * startup and proper functioning of the application together. You must register value readers with this context
 * via {@link #addValueReader(ValueReader)}, actions via {@link #addAction(String, Object)}, and strategies
 * via {@link #addDisassembleStrategy(DisassembleStrategy)}.
 *
 * You can also register post processors using {@link #addBeanProcessor(BeanProcessor)}
 *
 * To execute the application, you must access the {@link Executor} indirectly via one of the two end-points:
 * <ul>
 *     <li>{@link #execute(java.io.PrintStream, String...)}</li>
 *     <li>{@link #execute(String...)}</li>
 * </ul>
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/11, 14:30)
 */
public class ExecutorContext extends AbstractThreadSafeContext<Object> {

    private final DisassembleStrategyContext strategyContext;
    private final ValueReaderContext valueReaderContext;
    private final ActionContext actionRegistry;
    private final Executor executor;

    public ExecutorContext() {
        valueReaderContext = new DefaultValueReaderContext();
        strategyContext = new DisassembleStrategyContext();
        actionRegistry = new ActionContext(strategyContext);
        executor = new Executor(actionRegistry);
        try {
            registerBean(this, valueReaderContext);
            registerBean(this, strategyContext);
            registerBean(this, actionRegistry);
            addDisassembleStrategy(new AnnotatedFieldsDisassembleStrategy());
            addValueReader(new ClassValueReader());
            addValueReader(new DateValueReader());
            addValueReader(new EnumValueReader());
            addValueReader(new FileValueReader());
            addValueReader(new PrimitiveValueReader());
            addValueReader(new UrlValueReader());
            addBeanProcessor(new AbstractBeanProcessor(0) {
                @Override
                public Object postProcessBeforeDispense(Object bean, String beanName) throws RegistryException {
                    if (bean instanceof ValueReaderAware) {
                        ValueReaderAware aware = (ValueReaderAware) bean;
                        aware.setValueReader(valueReaderContext);
                    }
                    return bean;
                }
            });
        } catch (RegistryException ignored) {
        }
    }

    protected void addBean(String name, Object bean) throws RegistryException {
        if (name == null) {
            name = bean.getClass().getName() + "#" + getMap().size();
        } else if (name.equals(bean.getClass().getCanonicalName())) {
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

    public DisassembleStrategyContext getStrategyContext() {
        return strategyContext;
    }

    public ValueReaderContext getValueReaderContext() {
        return valueReaderContext;
    }

    public ActionContext getActionRegistry() {
        return actionRegistry;
    }

    public void execute(PrintStream out, String... args) throws Exception {
        executor.execute(out, args);
    }

    public void execute(String... args) throws Exception {
        executor.execute(args);
    }

    @Override
    public void addBeanProcessor(BeanProcessor beanProcessor) {
        valueReaderContext.addBeanProcessor(beanProcessor);
        actionRegistry.addBeanProcessor(beanProcessor);
        strategyContext.addBeanProcessor(beanProcessor);
        super.addBeanProcessor(beanProcessor);
    }
}
