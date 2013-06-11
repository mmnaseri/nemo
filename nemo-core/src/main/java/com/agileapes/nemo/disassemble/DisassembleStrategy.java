package com.agileapes.nemo.disassemble;

import com.agileapes.nemo.contract.Executable;
import com.agileapes.nemo.error.NoSuchOptionException;
import com.agileapes.nemo.error.OptionDefinitionException;
import com.agileapes.nemo.option.OptionDescriptor;

import java.io.PrintStream;
import java.util.Set;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 16:52)
 */
public interface DisassembleStrategy<A> {

    OptionDescriptor getOption(A action, String option) throws NoSuchOptionException, OptionDefinitionException;

    OptionDescriptor getOption(A action, Character alias) throws NoSuchOptionException, OptionDefinitionException;

    OptionDescriptor getOption(A action, Integer index) throws NoSuchOptionException, OptionDefinitionException;

    Set<? extends OptionDescriptor> getOptions(A action) throws OptionDefinitionException;

    void setOption(A action, OptionDescriptor descriptor, String value) throws NoSuchOptionException, OptionDefinitionException;

    void reset(A action) throws OptionDefinitionException;

    boolean isDefaultAction(A action);

    boolean isInternal(A action);

    void setOutput(A action, PrintStream output);

    Executable getExecutable(A action);

    boolean accepts(Object action);

}
