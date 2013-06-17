package com.agileapes.nemo.demo;

import com.agileapes.nemo.events.translated.ExecutionStartedEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/16/13, 4:02 PM)
 */
public class HelkRedirector implements ApplicationListener<ExecutionStartedEvent> {

    @Override
    public void onApplicationEvent(ExecutionStartedEvent event) {
        final String[] arguments = event.getArguments();
        if (arguments.length > 0 && arguments[0].equals("helk")) {
            event.getOutput().println("[NOTE] You typed 'helk'. We will assume that you meant 'help'.\n");
            arguments[0] = "help";
            event.setArguments(arguments);
        }
    }

}
