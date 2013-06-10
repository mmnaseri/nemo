package com.agileapes.nemo.exec;

import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.action.ActionRegistry;
import com.agileapes.nemo.action.SmartAction;
import com.agileapes.nemo.disassemble.DisassembleStrategyContext;
import com.agileapes.nemo.disassemble.impl.AnnotatedFieldsDisassembleStrategy;
import com.agileapes.nemo.option.Options;
import com.agileapes.nemo.value.impl.DefaultValueReaderContext;
import com.agileapes.nemo.value.impl.PrimitiveValueReader;

import java.io.PrintStream;
import java.util.Map;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 19:50)
 */
public class Executor {

    private final ActionRegistry actionRegistry;
    private Execution execution;
    private PrintStream output;

    public Executor(ActionRegistry actionRegistry) {
        this.actionRegistry = actionRegistry;
    }

    public Execution getExecution() {
        return execution;
    }

    public PrintStream getOutput() {
        return output;
    }

    public void execute(String... args) throws Exception {
        execute(System.out, args);
    }

    public void execute(PrintStream output, String... args) throws Exception {
        this.output = output;
        execution = new Execution(actionRegistry, args);
        perform(execution);
    }

    public void perform(Execution execution) throws Exception {
        final SmartAction action = (SmartAction) actionRegistry.get(execution.getTarget());
        action.setOutput(output);
        action.reset();
        final Options options = execution.getOptions();
        for (Map.Entry<String, String> entry : options.getOptions().entrySet()) {
            action.setOption(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Character, String> entry : options.getAliases().entrySet()) {
            action.setOption(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Integer, String> entry : options.getIndexes().entrySet()) {
            action.setOption(entry.getKey(), entry.getValue());
        }
        action.execute();
    }

    public static void main(String[] args) throws Exception {
        final DisassembleStrategyContext strategyContext = new DisassembleStrategyContext();
        final AnnotatedFieldsDisassembleStrategy item = new AnnotatedFieldsDisassembleStrategy();
        final DefaultValueReaderContext valueReaderContext = new DefaultValueReaderContext();
        valueReaderContext.register(PrimitiveValueReader.class.getCanonicalName(), new PrimitiveValueReader());
        item.setValueReaderContext(valueReaderContext);
        strategyContext.register(AnnotatedFieldsDisassembleStrategy.class.getCanonicalName(), item);
        final ActionRegistry actionRegistry = new ActionRegistry(strategyContext);
        actionRegistry.register("help", new Action() {
            @Override
            public void execute() throws Exception {
                getOutput().println("Hello world!");
            }
        });
        final Executor executor = new Executor(actionRegistry);
        executor.execute("help", "--123");
    }

}
