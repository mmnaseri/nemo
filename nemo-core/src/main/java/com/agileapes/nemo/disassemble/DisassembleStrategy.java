package com.agileapes.nemo.disassemble;

import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.contract.Executable;
import com.agileapes.nemo.option.OptionDescriptor;

import java.io.PrintStream;
import java.util.Set;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 16:52)
 */
public interface DisassembleStrategy<A> {

    OptionDescriptor getOption(A action, String option);

    OptionDescriptor getOption(A action, Character alias);

    OptionDescriptor getOption(A action, Integer index);

    Set<OptionDescriptor> getOptions(A action);

    void setOption(A action, OptionDescriptor descriptor, String value);

    void reset(A action);

    boolean isDefaultAction(A action);

    boolean isInternal(A action);

    void setOutput(A action, PrintStream output);

    Executable getExecutable(A action);

    boolean accepts(Object action);

}
