package com.agileapes.nemo.disassemble.impl;

import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.api.Disassembler;
import com.agileapes.nemo.api.Option;
import com.agileapes.nemo.contract.Callback;
import com.agileapes.nemo.contract.Executable;
import com.agileapes.nemo.error.OptionDefinitionException;
import com.agileapes.nemo.error.WrappedError;
import com.agileapes.nemo.option.OptionDescriptor;
import com.agileapes.nemo.util.FieldAnnotationFilter;
import com.agileapes.nemo.util.NonStaticFieldFilter;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static com.agileapes.nemo.util.ReflectionUtils.withFields;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 17:59)
 */
public class AnnotatedFieldsDisassembleStrategy extends AbstractCachingDisassembleStrategy<Action, AnnotatedFieldsDisassembleStrategy.FieldOptionDescriptor> {

    @Override
    protected Set<FieldOptionDescriptor> describe(final Action action) throws OptionDefinitionException {
        final HashSet<FieldOptionDescriptor> descriptors = new HashSet<FieldOptionDescriptor>();
        try {
            withFields(action.getClass())
                    .filter(new FieldAnnotationFilter(Option.class))
                    .filter(new NonStaticFieldFilter())
                    .each(new Callback<Field>() {
                        @Override
                        public void perform(Field field) {
                            field.setAccessible(true);
                            final String propertyName = field.getName();
                            final Option annotation = field.getAnnotation(Option.class);
                            try {
                                descriptors.add(new FieldOptionDescriptor(propertyName, annotation.alias() != ' ' ? annotation.alias() : null, annotation.index() >= 0 ? annotation.index() : null, annotation.required(), field.getType(), field.get(action), field));
                            } catch (Throwable e) {
                                throw new WrappedError(e);
                            }
                        }
                    });
        } catch (WrappedError e) {
            throw new OptionDefinitionException("Could not get a description for option", e.getWrappedError(Throwable.class));
        }
        return descriptors;
    }

    @Override
    protected void setOption(Action action, FieldOptionDescriptor target, Object converted) {
        try {
            target.getField().set(action, converted);
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean isDefaultAction(Action action) {
        return action.isDefaultAction();
    }

    @Override
    public boolean isInternal(Action action) {
        return action.isInternal();
    }

    @Override
    public void setOutput(Action action, PrintStream output) {
        action.setOutput(output);
    }

    @Override
    public Executable getExecutable(Action action) {
        return action;
    }

    @Override
    public boolean accepts(Object action) {
        return action instanceof Action && (!action.getClass().isAnnotationPresent(Disassembler.class) || action.getClass().getAnnotation(Disassembler.class).value().equals(getClass()));
    }

    public final static class FieldOptionDescriptor extends OptionDescriptor {

        private final Field field;

        public FieldOptionDescriptor(String name, Character alias, Integer index, boolean required, Class<?> type, Object defaultValue, Field field) throws OptionDefinitionException {
            super(name, alias, index, required, type, defaultValue);
            this.field = field;
        }

        public Field getField() {
            return field;
        }

    }

}
