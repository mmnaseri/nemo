package com.agileapes.nemo.demo;

import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.api.Command;
import com.agileapes.nemo.api.Help;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/15/13, 5:29 PM)
 */
@Command("name")
@Help("Will greet the runner")
public class HelloAction extends Action {

    private String name = "Mickey";

    @Help("Tells the application whom to say hello to")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void execute() throws Exception {
        output.println("Hello, " + name);
    }

}
