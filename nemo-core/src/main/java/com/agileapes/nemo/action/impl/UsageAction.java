package com.agileapes.nemo.action.impl;

import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.action.ActionContextAware;
import com.agileapes.nemo.api.Disassembler;
import com.agileapes.nemo.api.Option;
import com.agileapes.nemo.contract.Callback;
import com.agileapes.nemo.contract.Filter;
import com.agileapes.nemo.disassemble.impl.AnnotatedFieldsDisassembleStrategy;
import com.agileapes.nemo.option.OptionDescriptor;
import com.agileapes.nemo.util.CollectionDSL;

import java.io.PrintStream;
import java.util.*;

import static com.agileapes.nemo.util.CollectionDSL.with;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/13/13, 1:46 PM)
 */
@Disassembler(AnnotatedFieldsDisassembleStrategy.class)
public class UsageAction extends Action implements ActionContextAware {

    private static final String ALL = "*";

    @Option(index = 0)
    private String target = ALL;
    private ActionContext actionContext;

    @Override
    public void setActionContext(ActionContext actionContext) {
        this.actionContext = actionContext;
    }

    @Override
    public void execute() throws Exception {
        if (ALL.equals(target)) {
            output.print("Usage: %APPLICATION%");
            final List<String> list = CollectionDSL.sorted(with(actionContext.getTargets())
                    .filter(new Filter<String>() {
                        @Override
                        public boolean accepts(String item) {
                            for (Action action : actionContext.getInternalActions()) {
                                if (action.getName().equals(item)) {
                                    return false;
                                }
                            }
                            return true;
                        }
                    })
                    .list());
            if (!list.isEmpty()) {
                output.print(" ");
                for (Iterator<String> iterator = list.iterator(); iterator.hasNext(); ) {
                    String action = iterator.next();
                    output.print(action);
                    if (iterator.hasNext()) {
                        output.print("|");
                    }
                }
                output.print(" <options>");
            }
            output.println();
        } else {
            final SmartAction<?> action = (SmartAction) actionContext.get(target);
            final ArrayList<OptionDescriptor> descriptors = new ArrayList<OptionDescriptor>(action.getOptions());
            Collections.sort(descriptors, new Comparator<OptionDescriptor>() {
                @Override
                public int compare(OptionDescriptor o1, OptionDescriptor o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            final DescriptorWriter optionWriter = new DescriptorWriter(output) {
                @Override
                protected void write(OptionDescriptor item) {
                    if (item.getIndex() != null) {
                        output.print("<" + item.getName() + ">");
                    } else {
                        output.print("--" + item.getName() + (item.isFlag() ? "" : " <value>"));
                    }
                }
            };
            output.print("%APPLICATION% " + target + " ");
            final CollectionDSL.Wrapper<OptionDescriptor> flags = with(descriptors)
                    .filter(new Filter<OptionDescriptor>() {
                        @Override
                        public boolean accepts(OptionDescriptor item) {
                            return item.isFlag();
                        }
                    });
            flags.each(optionWriter);
            with(descriptors)
                    .filter(new Filter<OptionDescriptor>() {
                        @Override
                        public boolean accepts(OptionDescriptor item) {
                            return !item.isFlag() && item.getIndex() == null;
                        }
                    })
                    .each(optionWriter);
            final CollectionDSL.Wrapper<OptionDescriptor> indexed = with(descriptors)
                    .filter(new Filter<OptionDescriptor>() {
                        @Override
                        public boolean accepts(OptionDescriptor item) {
                            return item.getIndex() != null;
                        }
                    });
            indexed.each(optionWriter).count();
            output.println();
            if (flags.count() > 0) {
                output.println("You can set values for flags by giving them values, e.g. --" + flags.first().getName() + " false");
            }
            if (indexed.count() > 0) {
                output.println("Options that are addressed sequentially (e.g. <" + indexed.first().getName() + ">) can be addressed " +
                        "directly as well, e.g. --" + indexed.last().getName() + " <value>");
                output.println("This is useful if you want to, for instance, set the value of the second option while " +
                        "leaving the first unset.");
            }

        }
    }

    private static abstract class DescriptorWriter implements Callback<OptionDescriptor> {

        protected final PrintStream output;

        protected DescriptorWriter(PrintStream output) {
            this.output = output;
        }

        @Override
        public void perform(OptionDescriptor item) {
            if (!item.isRequired()) {
                output.print("[");
            }
            write(item);
            if (!item.isRequired()) {
                output.print("]");
            }
            output.print(" ");
        }

        protected abstract void write(OptionDescriptor item);

    }

}
