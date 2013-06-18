package com.agileapes.nemo.demo;

import com.agileapes.nemo.events.translated.ExecutionStartedEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/18/13, 8:45 AM)
 */
public class ReadActionAlias implements ApplicationListener<ExecutionStartedEvent> {

    @Override
    public void onApplicationEvent(ExecutionStartedEvent event) {
        final String[] arguments = event.getArguments();
        if (arguments.length == 0 || arguments[0] == null || arguments[0].isEmpty() || arguments[0].startsWith("-")) {
            return;
        }
        if (arguments[0].equals("cat")) {
            arguments[0] = "read";
        }
        event.setArguments(arguments);
    }

}
