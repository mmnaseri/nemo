package com.agileapes.nemo.error;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/15/13, 6:43 PM)
 */
public class CommandSyntaxError extends Exception {

    public CommandSyntaxError(int position, String msg) {
        super("Syntax error at " + position + ": " + msg);
    }

}
