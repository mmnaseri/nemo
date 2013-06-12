package com.agileapes.nemo.disassemble;

import com.agileapes.nemo.contract.Executable;
import com.agileapes.nemo.error.NoSuchOptionException;
import com.agileapes.nemo.error.OptionDefinitionException;
import com.agileapes.nemo.option.OptionDescriptor;

import java.io.PrintStream;
import java.util.Set;

/**
 * This interface abstracts the basic actions that are to be taken by any given strategy to disassemble and configure the
 * underlying actions
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 16:52)
 */
public interface DisassembleStrategy<A> {

    /**
     * This method will try to describe the option that has the given name
     * @param action    the action
     * @param option    the name of the option
     * @return te descriptor for this option
     * @throws NoSuchOptionException
     * @throws OptionDefinitionException
     */
    OptionDescriptor getOption(A action, String option) throws NoSuchOptionException, OptionDefinitionException;


    /**
     * This method will try to describe the option that has the given alias
     * @param action    the action
     * @param alias    the alias of the option
     * @return te descriptor for this option
     * @throws NoSuchOptionException
     * @throws OptionDefinitionException
     */
    OptionDescriptor getOption(A action, Character alias) throws NoSuchOptionException, OptionDefinitionException;

    /**
     * This method will try to describe the option that has the given index
     * @param action    the action
     * @param index    the index of the option
     * @return te descriptor for this option
     * @throws NoSuchOptionException
     * @throws OptionDefinitionException
     */
    OptionDescriptor getOption(A action, Integer index) throws NoSuchOptionException, OptionDefinitionException;

    /**
     * This method will return the set of all options for the given action
     * @param action    the action
     * @return the set of options for this action
     * @throws OptionDefinitionException
     */
    Set<? extends OptionDescriptor> getOptions(A action) throws OptionDefinitionException;

    /**
     * This method will try to set the value for the option as described by the descriptor passed through to the method
     * @param action        the action for which the option is being set
     * @param descriptor    the descriptor for the option
     * @param value         the textual representation of the value for this option
     * @throws NoSuchOptionException
     * @throws OptionDefinitionException
     */
    void setOption(A action, OptionDescriptor descriptor, String value) throws NoSuchOptionException, OptionDefinitionException;

    /**
     * This method will reset all the options for this action to their default values
     * @param action    the action
     * @throws OptionDefinitionException
     */
    void reset(A action) throws OptionDefinitionException;

    /**
     * Will determine whether the action is the default action or not
     * @param action    the action
     * @return {@code true} if this action is the default action
     */
    boolean isDefaultAction(A action);

    /**
     * This method determines whether the given action is an internal action
     * @param action    the action
     * @return {@code true} if this action is an internal action
     */
    boolean isInternal(A action);

    /**
     * This method will change the output for the given action
     * @param action    the action
     * @param output    the output to which the action should write its output messages
     */
    void setOutput(A action, PrintStream output);

    /**
     * This method will return an {@link Executable} object which, once executed, will carry out the task encapsulated
     * by the underlying action
     * @param action    the action
     * @return The executable representing the unit of work that should be carried out by the action
     */
    Executable getExecutable(A action);

    /**
     * This method will determine whether the Strategy-Action end of the trust relationship holds; i.e., it will determine
     * whether this strategy can disassemble the given action object
     * @param action    the action
     * @return {@code true} if this strategy can manage the task of disassembling the action into its descriptive pieces
     */
    boolean accepts(Object action);

}
