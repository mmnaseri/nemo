package com.agileapes.nemo.error;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 16:42)
 */
public class DuplicateItemException extends RegistryException {

    public DuplicateItemException(String name) {
        super("Another item with the name <" + name + "> has already been registered");
    }

}
