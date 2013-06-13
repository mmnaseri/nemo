package com.agileapes.nemo.action.impl;

import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.disassemble.DisassembleStrategy;
import com.agileapes.nemo.disassemble.DisassemblerAware;
import com.agileapes.nemo.error.NoSuchOptionException;
import com.agileapes.nemo.error.OptionDefinitionException;
import com.agileapes.nemo.error.RequiredOptionsMissingException;
import com.agileapes.nemo.option.OptionDescriptor;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * The smart action class is an extension to the action class which is able to discern much information through
 * the strategy it takes in. This class takes a generically typed action object and an associated strategy and by
 * adding a level of indirection will enable you to get access to much of the internally available data and metadata
 * associated with actions throughout the execution of the system.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 17:18)
 */
public class SmartAction<A> extends Action implements DisassemblerAware<A> {

    private final A action;
    private final DisassembleStrategy<A> strategy;
    private final Set<OptionDescriptor> required = new HashSet<OptionDescriptor>();

    /**
     * Will instantiate the smart action by getting the action to be wrapped and its associated strategy
     * @param action      the action
     * @param strategy    the strategy
     */
    public SmartAction(A action, DisassembleStrategy<A> strategy) {
        this.action = action;
        this.strategy = strategy;
    }

    /**
     * Will set the option described by the {@code described} parameter
     * @param descriptor    the descriptor
     * @param value         the textual representation of the desired target value
     * @throws NoSuchOptionException if the descriptor does not describe an option associated with this action
     * @throws OptionDefinitionException if there is a problem with the definition of the option itself
     */
    private void setOption(OptionDescriptor descriptor, String value) throws NoSuchOptionException, OptionDefinitionException {
        required.remove(descriptor);
        strategy.setOption(action, descriptor, value);
    }

    /**
     * Will attempt to set the option by name
     * @param option        the name of the option (--name)
     * @param value         the textual representation of the desired target value
     * @throws NoSuchOptionException
     * @throws OptionDefinitionException
     */
    public void setOption(String option, String value) throws NoSuchOptionException, OptionDefinitionException {
        setOption(strategy.getOption(action, option), value);
    }

    /**
     * Will attempt to set the option value by alias
     * @param alias         the alias of the option (-alias)
     * @param value         the textual representation of the desired target value
     * @throws NoSuchOptionException
     * @throws OptionDefinitionException
     */
    public void setOption(Character alias, String value) throws NoSuchOptionException, OptionDefinitionException {
        setOption(strategy.getOption(action, alias), value);
    }

    /**
     * Will attempt to set the option value by numerical index
     * @param index         the index to the option (%index)
     * @param value         the textual representation of the desired target value
     * @throws NoSuchOptionException
     * @throws OptionDefinitionException
     */
    public void setOption(Integer index, String value) throws NoSuchOptionException, OptionDefinitionException {
        setOption(strategy.getOption(action, index), value);
    }

    /**
     * @return a set of all the options provided by the action
     * @throws OptionDefinitionException
     */
    public Set<? extends OptionDescriptor> getOptions() throws OptionDefinitionException {
        return strategy.getOptions(action);
    }

    /**
     * Will reset all option values to the defaults designated by action developers.
     * @throws OptionDefinitionException
     */
    public void reset() throws OptionDefinitionException {
        strategy.reset(action);
    }

    /**
     * @return {@code true} if this action is the default action. Note that in all the context of the executor
     * (as denoted by {@link com.agileapes.nemo.exec.ExecutorContext}) only one action can be marked as the default
     */
    @Override
    public boolean isDefaultAction() {
        return strategy.isDefaultAction(action);
    }

    /**
     * @return {@code true} means that this action is an internal action. Internal actions cannot be invoked by
     * the command line, and are meant as a way to break down the work structure into more manageable pieces in case
     * actions are too big or share sub-actions as pieces of work to be done
     */
    @Override
    public boolean isInternal() {
        return strategy.isInternal(action);
    }

    /**
     * @return the strategy used to disassemble the internally wrapped action
     */
    @Override
    public DisassembleStrategy<A> getDisassembler() {
        return strategy;
    }

    /**
     * @return the wrapped action. Note that modifications to this object might change the behaviour of the system in a
     * non-deterministic manner.
     */
    public A getAction() {
        return action;
    }

    /**
     * @return the metadata associated with this action
     */
    public Properties getMetadata() {
        return strategy.getMetadata(action);
    }

    /**
     * Will execute the action after determining that all of its required options have been set.
     * @throws RequiredOptionsMissingException if one or more of the required options have not been set.
     * Remember that marking an option as required is at odds with providing a default value for it. Options
     * that are required will be marked as not-set even if they have default values so long as they have not
     * been configured externally
     * @throws Exception
     */
    @Override
    public void execute() throws Exception {
        if (!required.isEmpty()) {
            throw new RequiredOptionsMissingException(required.toArray(new OptionDescriptor[required.size()]));
        }
        strategy.setOutput(action, getOutput());
        strategy.getExecutable(action).execute();
    }

}
