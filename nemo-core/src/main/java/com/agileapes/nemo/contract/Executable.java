package com.agileapes.nemo.contract;

/**
 * This interface signifies an entity with a run-once task that can be performed by invoking the {@link #execute()}
 * method
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 16:39)
 */
public interface Executable {

    /**
     * This method will be called from the outside and will run without accepting any parameters
     * @throws Exception in case of any problems preventing a successful execution of the executable
     */
    void execute() throws Exception;

}
