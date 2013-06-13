package com.agileapes.nemo.exec;

import com.agileapes.nemo.action.impl.ActionContext;
import com.agileapes.nemo.error.InvalidArgumentSyntaxException;
import com.agileapes.nemo.error.NoDefaultActionException;
import com.agileapes.nemo.option.Options;

/**
 * The execution abstracts the meaning of an action being invoked from the command line.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 21:45)
 */
public class Execution {

    private String target;
    private final String[] arguments;
    private final Options options;

    Execution(ActionContext actionRegistry, String... arguments) throws NoDefaultActionException, InvalidArgumentSyntaxException {
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
