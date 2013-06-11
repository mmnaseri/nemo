package com.agileapes.nemo.exec;

import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.action.ActionRegistry;
import com.agileapes.nemo.action.SmartAction;
import com.agileapes.nemo.api.Option;
import com.agileapes.nemo.disassemble.impl.AnnotatedFieldsDisassembleStrategy;
import com.agileapes.nemo.option.Options;
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
        if (action.isInternal()) {
            throw new IllegalAccessException("Internal action '" + execution.getTarget() + "' cannot be called from the command line");
        }
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
        final Action action = new Action() {

            @Option(alias = 'n')
            private String name = "Mickey";

            @Option(index = 0)
            private String nickname;

            @Override
            public void execute() throws Exception {
                getOutput().print("Hello, " + name);
                if (nickname != null) {
                    getOutput().print(", the " + nickname);
                }
                getOutput().println();
            }
        };
        action.setDefaultAction(true);
        final ExecutorContext context = new ExecutorContext();
        context.addDisassembleStrategy(new AnnotatedFieldsDisassembleStrategy());
        context.addValueReader(new PrimitiveValueReader());
        context.addAction("hail", action);
        context.execute();
    }

}
