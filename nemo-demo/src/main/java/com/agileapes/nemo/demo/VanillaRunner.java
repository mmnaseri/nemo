package com.agileapes.nemo.demo;

import com.agileapes.nemo.assets.HelpAction;
import com.agileapes.nemo.assets.TypoCorrectionAsset;
import com.agileapes.nemo.assets.UsageAction;
import com.agileapes.nemo.exec.ExecutorContext;
import com.agileapes.nemo.util.ExceptionMessage;

import static com.agileapes.nemo.exec.ExecutorContext.withActions;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/11/11, 15:52)
 */
public class VanillaRunner {

    public static void main(String[] args) throws Exception {
        try {
            final ExecutorContext context = withActions(UsageAction.class, ListAction.class, ReadAction.class, HelpAction.class, HelloAction.class);
            context.addEventListener(new TypoCorrectionAsset(0.3));
            context.execute(args);
        } catch (Throwable e) {
            System.err.println("error: " + new ExceptionMessage(e).getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

}
