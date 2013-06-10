package com.agileapes.nemo.disassemble.impl;

import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.contract.Executable;
import com.agileapes.nemo.disassemble.DisassembleStrategy;
import com.agileapes.nemo.option.OptionDescriptor;

import java.io.PrintStream;
import java.util.Set;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 17:59)
 */
public class AnnotatedSettersDisassembleStrategy implements DisassembleStrategy<Action> {
    @Override
    public OptionDescriptor getOption(Action action, String option) {
        return null;
    }

    @Override
    public OptionDescriptor getOption(Action action, Character alias) {
        return null;
    }

    @Override
    public OptionDescriptor getOption(Action action, Integer index) {
        return null;
    }

    @Override
    public Set<OptionDescriptor> getOptions(Action action) {
        return null;
    }

    @Override
    public void setOption(Action action, OptionDescriptor descriptor, String value) {
    }

    @Override
    public void reset(Action action) {
    }

    @Override
    public boolean isDefaultAction(Action action) {
        return false;
    }

    @Override
    public boolean isInternal(Action action) {
        return false;
    }

    @Override
    public void setOutput(Action action, PrintStream output) {
    }

    @Override
    public Executable getExecutable(Action action) {
        return null;
    }

    @Override
    public boolean accepts(Object action) {
        return false;
    }
}
