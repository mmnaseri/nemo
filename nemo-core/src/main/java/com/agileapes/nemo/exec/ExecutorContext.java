package com.agileapes.nemo.exec;

import com.agileapes.couteau.context.contract.BeanProcessor;
import com.agileapes.couteau.context.contract.Context;
import com.agileapes.couteau.context.contract.Registry;
import com.agileapes.couteau.context.error.FatalRegistryException;
import com.agileapes.couteau.context.error.RegistryException;
import com.agileapes.couteau.context.impl.AbstractThreadSafeContext;
import com.agileapes.couteau.context.impl.BeanProcessorAdapter;
import com.agileapes.couteau.context.value.ValueReader;
import com.agileapes.couteau.context.value.ValueReaderAware;
import com.agileapes.couteau.context.value.ValueReaderContext;
import com.agileapes.couteau.context.value.impl.*;
import com.agileapes.couteau.reflection.util.ClassUtils;
import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.action.ActionContextAware;
import com.agileapes.nemo.action.impl.ActionContext;
import com.agileapes.nemo.disassemble.DisassembleStrategy;
import com.agileapes.nemo.disassemble.impl.AnnotatedFieldsDisassembleStrategy;
import com.agileapes.nemo.disassemble.impl.CommandStatementDisassembleStrategy;
import com.agileapes.nemo.disassemble.impl.DisassembleStrategyContext;
import com.agileapes.nemo.event.impl.events.ExecutionErrorEvent;
import com.agileapes.nemo.event.impl.events.ExecutionStartedEvent;
import com.agileapes.nemo.util.ExceptionMessage;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.PrintStream;
import java.util.Arrays;

/**
 * The executor context is the central context which holds all the other pieces of information required for the
 * startup and proper functioning of the application together. You must register value readers with this context
 * via {@link #addValueReader(ValueReader)}, actions via {@link #addAction(String, Object)}, and strategies
 * via {@link #addDisassembleStrategy(DisassembleStrategy)}.
 *
 * You can also register post processors using {@link #addBeanProcessor(com.agileapes.couteau.context.contract.BeanProcessor)}
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

    public static final String ACTION_SUFFIX = "Action";
    private final DisassembleStrategyContext strategyContext;
    private final ValueReaderContext valueReaderContext;
    private final ActionContext actionContext;
    private final Executor executor;
    private final static Log log = LogFactory.getLog(ExecutorContext.class);
    private PrintStream output;

    private static Object instantiateAction(Class<?> action) throws FatalRegistryException {
        final Object instance;
        try {
            instance = action.newInstance();
        } catch (Exception e) {
            throw new FatalRegistryException("Failed to instantiate action", e);
        }
        return instance;
    }

    private static String getActionName(Class<?> action) {
        String name = action.getSimpleName();
        if (name.endsWith(ACTION_SUFFIX)) {
            name = name.substring(0, name.length() - ACTION_SUFFIX.length());
        }
        name = StringUtils.uncapitalize(name);
        return name;
    }

    public static ExecutorContext withActions(Object... actions) throws RegistryException {
        final ExecutorContext executorContext = new ExecutorContext();
        for (Object action : actions) {
            executorContext.addAction(getActionName(action.getClass()), action);
        }
        return executorContext;
    }

    public static ExecutorContext withActions(Class<? extends Action> defaultAction, Class... actions) throws RegistryException {
        final ExecutorContext executorContext = new ExecutorContext();
        executorContext.addDefaultAction(defaultAction);
        for (Class action : actions) {
            executorContext.addAction(action);
        }
        return executorContext;
    }

    public ExecutorContext() {
        log.info("Starting executor context ...");
        final long time = System.currentTimeMillis();
        valueReaderContext = new DefaultValueReaderContext();
        strategyContext = new DisassembleStrategyContext();
        actionContext = new ActionContext(strategyContext);
        executor = new Executor(actionContext, this);
        try {
            registerBean(this, valueReaderContext);
            registerBean(this, strategyContext);
            registerBean(this, actionContext);
            addDisassembleStrategy(new AnnotatedFieldsDisassembleStrategy());
            addDisassembleStrategy(new CommandStatementDisassembleStrategy());
            addValueReader(new ClassValueReader());
            addValueReader(new DateValueReader());
            addValueReader(new EnumValueReader());
            addValueReader(new FileValueReader());
            addValueReader(new PrimitiveValueReader());
            addValueReader(new UrlValueReader());
            addBeanProcessor(new BeanProcessorAdapter<Object>() {
                @Override
                public Object postProcessBeforeAccess(Object bean, String beanName) throws RegistryException {
                    if (bean instanceof ValueReaderAware) {
                        log.info("Injecting value reader context to bean: " + beanName);
                        ((ValueReaderAware) bean).setValueReader(valueReaderContext);
                    }
                    return bean;
                }

            });
            addBeanProcessor(new BeanProcessorAdapter<Object>() {
                @Override
                public Object postProcessBeforeAccess(Object bean, String beanName) throws RegistryException {
                    if (bean instanceof ActionContextAware) {
                        log.info("Injecting action context to bean: " + beanName);
                        ((ActionContextAware) bean).setActionContext(actionContext);
                    }
                    return bean;
                }
            });
            addBeanProcessor(new BeanProcessorAdapter<Object>() {
                @Override
                public Object postProcessBeforeRegistration(Object bean, String beanName) throws RegistryException {
                    if (bean instanceof ExecutorAware) {
                        ((ExecutorAware) bean).setExecutor(executor);
                    }
                    return bean;
                }
            });
        } catch (RegistryException ignored) {
        }
        ready();
        log.info("Bootstrapping took " + (System.currentTimeMillis() - time) + "ms");
        log.info("System startup date is " + getStartupDate());
    }

    protected void addBean(String name, Object bean) throws RegistryException {
        if (name == null) {
            name = bean.getClass().getName() + "#" + getBeanNames().size();
        } else if (name.equals(bean.getClass().getCanonicalName())) {
            name = name + "#" + getBeanNames().size();
        }
        log.debug("Registering bean <" + name + ">");
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
        log.info("Adding value reader: " + valueReader.getClass().getCanonicalName());
        registerBean(valueReaderContext, valueReader);
    }

    public void addDisassembleStrategy(DisassembleStrategy strategy) throws RegistryException {
        log.info("Adding disassemble strategy: " + strategy.getClass().getCanonicalName());
        registerBean(strategyContext, strategy);
    }

    public void addAction(String target, Object action) throws RegistryException {
        log.info("Registering action target: " + target);
        registerBean(actionContext, target, action);
    }

    public void addAction(String target, Class<?> action) throws RegistryException{
        addAction(target, instantiateAction(action));
    }

    public void addDefaultAction(String target, Action action) throws RegistryException {
        action.setDefaultAction(true);
        addAction(target, action);
    }

    public void addDefaultAction(String target, Class<? extends Action> action) throws RegistryException {
        addDefaultAction(target, action.cast(instantiateAction(action)));
    }

    public void addAction(Class<?> action) throws RegistryException {
        addAction(getActionName(action), action);
    }

    public void addDefaultAction(Class<? extends Action> action) throws RegistryException {
        addDefaultAction(getActionName(action), action);
    }

    public DisassembleStrategyContext getStrategyContext() {
        return strategyContext;
    }

    public ValueReaderContext getValueReaderContext() {
        return valueReaderContext;
    }

    public ActionContext getActionContext() {
        return actionContext;
    }

    public void execute(String... args) throws Exception {
        execute(System.out, args);
    }

    public void execute(PrintStream out, String... args) throws Exception {
        log.info("Starting execution ...");
        log.debug("Provided arguments are: " + Arrays.toString(args));
        this.output = out;
        final ExecutionStartedEvent event = publishEvent(new ExecutionStartedEvent(this, args, out));
        args = event.getArguments();
        out = event.getOutput();
        if (System.out.equals(out)) {
            log.debug("Output is redirected to the standard output");
        }
        try {
            executor.execute(out, args);
        } catch (Exception e) {
            e = publishEvent(new ExecutionErrorEvent(this, e)).getError();
            log.error("Errors prevented the execution of the system");
            if (log.isDebugEnabled()) {
                log.error(new ExceptionMessage(e));
            }
            throw e;
        }
    }

    public PrintStream getOutput() {
        return output;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Context<Object> addBeanProcessor(BeanProcessor processor) {
        final Class<?> processorType = ClassUtils.resolveTypeArgument(processor.getClass(), BeanProcessor.class);
        if (strategyContext != null) {
            if (processorType.isAssignableFrom(strategyContext.getRegistryType())) {
                strategyContext.addBeanProcessor(processor);
            }
        }
        if (valueReaderContext != null) {
            if (processorType.isAssignableFrom(valueReaderContext.getRegistryType())) {
                valueReaderContext.addBeanProcessor(processor);
            }
        }
        if (actionContext != null) {
            if (processorType.isAssignableFrom(actionContext.getRegistryType())) {
                actionContext.addBeanProcessor(processor);
            }
        }
        return super.addBeanProcessor(processor);
    }
}
