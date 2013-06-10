package com.agileapes.nemo.exec;

import com.agileapes.nemo.action.ActionRegistry;
import com.agileapes.nemo.error.NoDefaultActionException;
import com.agileapes.nemo.option.Options;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 21:45)
 */
public class Execution {

    private String target;
    private final String[] arguments;
    private final Options options;

    Execution(ActionRegistry actionRegistry, String... arguments) throws NoDefaultActionException {
        if (arguments.length == 0 || arguments[0].startsWith("-")) {
            this.target = actionRegistry.getDefaultAction().getName();
            this.arguments = arguments;
        } else {
            this.target = arguments[0];
            this.arguments = new String[arguments.length - 1];
            System.arraycopy(arguments, 1, this.arguments, 0, arguments.length - 1);
        }
        this.options = new Options.Builder(this.arguments).build();
    }

    void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public String[] getArguments() {
        return arguments;
    }

    public Options getOptions() {
        return options;
    }

}
