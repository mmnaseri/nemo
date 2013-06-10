package com.agileapes.nemo.action;

import com.agileapes.nemo.disassemble.DisassembleStrategy;
import com.agileapes.nemo.disassemble.DisassemblerAware;
import com.agileapes.nemo.error.RequiredOptionsMissingException;
import com.agileapes.nemo.option.OptionDescriptor;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 17:18)
 */
public class SmartAction<A> extends Action implements DisassemblerAware<A> {

    private final A action;
    private final DisassembleStrategy<A> strategy;
    private final Set<OptionDescriptor> required = new HashSet<OptionDescriptor>();

    public SmartAction(A action, DisassembleStrategy<A> strategy) {
        this.action = action;
        this.strategy = strategy;
    }

    private void setOption(OptionDescriptor descriptor, String value) {
        required.remove(descriptor);
        strategy.setOption(action, descriptor, value);
    }

    public void setOption(String option, String value) {
        setOption(strategy.getOption(action, option), value);
    }

    public void setOption(Character alias, String value) {
        setOption(strategy.getOption(action, alias), value);
    }

    public void setOption(Integer index, String value) {
        setOption(strategy.getOption(action, index), value);
    }

    public Set<OptionDescriptor> getOptions() {
        return strategy.getOptions(action);
    }

    public void reset() {
        strategy.reset(action);
    }

    @Override
    public boolean isDefaultAction() {
        return strategy.isDefaultAction(action);
    }

    @Override
    public boolean isInternal() {
        return strategy.isInternal(action);
    }

    @Override
    public void execute() throws Exception {
        if (!required.isEmpty()) {
            throw new RequiredOptionsMissingException(required.toArray(new OptionDescriptor[required.size()]));
        }
        strategy.setOutput(action, getOutput());
        strategy.getExecutable(action).execute();
    }

    @Override
    public DisassembleStrategy<A> getDisassembler() {
        return strategy;
    }

}
