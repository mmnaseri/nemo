package com.agileapes.nemo.exec;

import com.agileapes.nemo.action.ActionContextAware;
import com.agileapes.nemo.action.impl.ActionContext;
import com.agileapes.nemo.contract.BeanProcessor;
import com.agileapes.nemo.contract.Registry;
import com.agileapes.nemo.contract.impl.AbstractBeanProcessor;
import com.agileapes.nemo.contract.impl.AbstractThreadSafeContext;
import com.agileapes.nemo.disassemble.DisassembleStrategy;
import com.agileapes.nemo.disassemble.impl.AnnotatedFieldsDisassembleStrategy;
import com.agileapes.nemo.disassemble.impl.CommandStatementDisassembleStrategy;
import com.agileapes.nemo.disassemble.impl.DisassembleStrategyContext;
import com.agileapes.nemo.error.RegistryException;
import com.agileapes.nemo.util.ExceptionMessage;
import com.agileapes.nemo.value.ValueReader;
import com.agileapes.nemo.value.ValueReaderAware;
import com.agileapes.nemo.value.ValueReaderContext;
import com.agileapes.nemo.value.impl.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;

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
    private final Date startupDate;
    private final static Log log = LogFactory.getLog(ExecutorContext.class);

    public ExecutorContext() {
        log.info("Starting executor context ...");
        final long time = System.currentTimeMillis();
        valueReaderContext = new DefaultValueReaderContext();
        strategyContext = new DisassembleStrategyContext();
        actionRegistry = new ActionContext(strategyContext);
        executor = new Executor(actionRegistry);
        try {
            registerBean(this, valueReaderContext);
            registerBean(this, strategyContext);
            registerBean(this, actionRegistry);
            addDisassembleStrategy(new AnnotatedFieldsDisassembleStrategy());
            addDisassembleStrategy(new CommandStatementDisassembleStrategy());
            addValueReader(new ClassValueReader());
            addValueReader(new DateValueReader());
            addValueReader(new EnumValueReader());
            addValueReader(new FileValueReader());
            addValueReader(new PrimitiveValueReader());
            addValueReader(new UrlValueReader());
            addBeanProcessor(new AbstractBeanProcessor() {
                @Override
                public Object postProcessBeforeDispense(Object bean, String beanName) throws RegistryException {
                    if (bean instanceof ValueReaderAware) {
                        log.info("Injecting value reader context to bean: " + beanName);
                        ((ValueReaderAware) bean).setValueReader(valueReaderContext);
                    }
                    return bean;
                }
            });
            addBeanProcessor(new AbstractBeanProcessor() {
                @Override
                public Object postProcessBeforeDispense(Object bean, String beanName) throws RegistryException {
                    if (bean instanceof ActionContextAware) {
                        log.info("Injecting action context to bean: " + beanName);
                        ((ActionContextAware) bean).setActionContext(actionRegistry);
                    }
                    return bean;
                }
            });
        } catch (RegistryException ignored) {
        }
        startupDate = new Date();
        log.info("Bootstrapping took " + (System.currentTimeMillis() - time) + "ms");
        log.info("System startup date is " + startupDate);
    }

    protected void addBean(String name, Object bean) throws RegistryException {
        if (name == null) {
            name = bean.getClass().getName() + "#" + getMap().size();
        } else if (name.equals(bean.getClass().getCanonicalName())) {
            name = name + "#" + getMap().size();
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
        log.info("Starting execution ...");
        log.debug("Provided arguments are: " + Arrays.toString(args));
        if (System.out.equals(out)) {
            log.debug("Output is redirected to the standard output");
        }
        try {
            executor.execute(out, args);
        } catch (Exception e) {
            log.error("Errors prevented the execution of the system");
            if (log.isDebugEnabled()) {
                log.error(new ExceptionMessage(e).getMessage());
            }
            throw e;
        }
    }

    public void execute(String... args) throws Exception {
        execute(System.out, args);
    }

    @Override
    protected Class<Object> getType() {
        return Object.class;
    }

    @Override
    public void addBeanProcessor(BeanProcessor beanProcessor) {
        log.debug("Registering bean processor with all available internal contexts");
        valueReaderContext.addBeanProcessor(beanProcessor);
        actionRegistry.addBeanProcessor(beanProcessor);
        strategyContext.addBeanProcessor(beanProcessor);
        super.addBeanProcessor(beanProcessor);
    }

    public Date getStartupDate() {
        return startupDate;
    }
}
