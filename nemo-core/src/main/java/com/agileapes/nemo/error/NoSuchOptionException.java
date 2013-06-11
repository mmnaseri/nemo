package com.agileapes.nemo.error;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/11, 15:09)
 */
public class NoSuchOptionException extends Exception {

    public NoSuchOptionException(String name) {
        super("No such option: --" + name);
    }

    public NoSuchOptionException(Character alias) {
        super("No such option: -" + alias);
    }

    public NoSuchOptionException(Integer index) {
        super("No such option: %" + index);
    }

}
