package com.agileapes.nemo.demo;

import com.agileapes.nemo.api.Command;
import com.agileapes.nemo.api.Help;

import java.io.PrintStream;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/15/13, 5:29 PM)
 */
@Command("action name")
@Help("Will greet the runner")
public class HelloAction {

    private PrintStream output;

    @Help("Tells the application whom to say hello to")
    private String name = "Mickey";

    public void execute() throws Exception {
        output.println("Hello, " + name);
    }

}
