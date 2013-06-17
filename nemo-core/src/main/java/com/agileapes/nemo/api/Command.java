package com.agileapes.nemo.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>@Command</code> annotation will be used to describe commands without having to
 * separately mark and sign all options.
 *
 * Option types are first evaluated against the properties they correspond to. Note that the existence of the
 * physical property inside the annotated class is not mandatory.
 *
 * @see com.agileapes.nemo.disassemble.impl.CommandStatementDisassembleStrategy The strategy for more information
 * on how this annotation is used
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/15/13, 4:53 PM)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {

    /**
     * @return the syntax to the command
     */
    String value();

}
