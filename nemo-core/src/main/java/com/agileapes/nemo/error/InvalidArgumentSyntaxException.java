package com.agileapes.nemo.error;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/12, 3:06)
 */
public class InvalidArgumentSyntaxException extends OptionDefinitionException {

    public InvalidArgumentSyntaxException(String argument) {
        super("Bad argument syntax: " + argument);
    }

    public InvalidArgumentSyntaxException(String name, String value) {
        super("Invalid value for argument --" + name + ": " + value);
    }

}
