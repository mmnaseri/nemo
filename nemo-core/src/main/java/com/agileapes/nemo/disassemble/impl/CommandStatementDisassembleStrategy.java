package com.agileapes.nemo.disassemble.impl;

import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.api.Command;
import com.agileapes.nemo.contract.Callback;
import com.agileapes.nemo.contract.Executable;
import com.agileapes.nemo.error.CommandSyntaxError;
import com.agileapes.nemo.error.OptionDefinitionException;
import com.agileapes.nemo.option.OptionDescriptor;
import com.agileapes.nemo.util.*;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static com.agileapes.nemo.util.ReflectionUtils.withFields;
import static com.agileapes.nemo.util.ReflectionUtils.withMethods;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/15/13, 4:53 PM)
 */
public class CommandStatementDisassembleStrategy extends AbstractCachingDisassembleStrategy<Action, CommandStatementDisassembleStrategy.AccessibleFieldOptionDescriptor> {

    @Override
    protected Set<AccessibleFieldOptionDescriptor> describe(Action action) throws OptionDefinitionException {
        final HashSet<AccessibleFieldOptionDescriptor> descriptors = new HashSet<AccessibleFieldOptionDescriptor>();
        final List<OptionDescriptor> options;
        try {
            options = new CommandParser(action.getClass().getAnnotation(Command.class)).getOptions();
        } catch (CommandSyntaxError e) {
            throw new OptionDefinitionException("Could not parse command definition", e);
        }
        for (OptionDescriptor option : options) {
            final Accessor<?> accessor = getAccessor(action.getClass(), option.getName());
            final Properties properties = new Properties();
            CollectionDSL.with(accessor.getAnnotations()).each(new Callback<Annotation>() {
                @Override
                public void perform(Annotation item) {
                    new AnnotationPropertyBuilder(item).addTo(properties);
                }
            });
            try {
                //noinspection unchecked
                descriptors.add(new AccessibleFieldOptionDescriptor(option.getName(), option.getAlias(), option.getIndex(), option.isRequired(), accessor.getType(), accessor.get(action), properties, accessor));
            } catch (Throwable e) {
                throw new OptionDefinitionException("Could not define option", e);
            }
        }
       return descriptors;
    }

    private Accessor<?> getAccessor(Class type, String property) throws OptionDefinitionException {
        final Object[] fields = withFields(type).filter(new NonStaticFieldFilter()).filter(new FieldNameFilter(property)).array();
        final Object[] getters = withMethods(type).filter(new GetterMethodFilter()).filter(new MethodPropertyFilter(property)).array();
        final Object[] setters = withMethods(type).filter(new SetterMethodFilter()).filter(new MethodPropertyFilter(property)).array();
        Object reader = null;
        Object writer = null;
        if (getters.length > 0) {
            reader = getters[0];
        } else {
            if (fields.length > 0) {
                reader = fields[0];
            }
        }
        if (setters.length > 0) {
            writer = setters[0];
        } else {
            if (fields.length > 0) {
                writer = fields[0];
            }
        }
        if (reader == null) {
            throw new OptionDefinitionException("No reader found for option: " + property);
        }
        if (writer == null) {
            throw new OptionDefinitionException("No writer found for option: " + property);
        }
        return new Accessor<Object>(reader, writer);
    }

    @Override
    protected void setOption(Action action, AccessibleFieldOptionDescriptor target, Object converted) {
        try {
            //noinspection unchecked
            target.getAccessor().set(action, converted);
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
    public Properties getMetadata(Action action) {
        final Properties properties = new Properties();
        CollectionDSL.with(action.getClass().getAnnotations())
                .each(new Callback<Annotation>() {
                    @Override
                    public void perform(Annotation item) {
                        new AnnotationPropertyBuilder(item).addTo(properties);
                    }
                });
        return properties;
    }

    @Override
    public boolean accepts(Object action) {
        return action instanceof Action && action.getClass().isAnnotationPresent(Command.class);
    }

    public static class AccessibleFieldOptionDescriptor<T> extends OptionDescriptor {

        private final Accessor<T> accessor;

        public AccessibleFieldOptionDescriptor(String name, Character alias, Integer index, boolean required, Class<T> type, T defaultValue, Properties properties, Accessor<T> accessor) throws OptionDefinitionException {
            super(name, alias, index, required, type, defaultValue, properties);
            this.accessor = accessor;
        }

        public Accessor<T> getAccessor() {
            return accessor;
        }

    }

    private static class Annotated {

        private final Object object;

        private Annotated(Object object) {
            this.object = object;
        }

        public Annotation[] getAnnotations() {
            if (object instanceof Method) {
                return ((Method) object).getAnnotations();
            } else if (object instanceof Field) {
                return ((Field) object).getAnnotations();
            }
            throw new IllegalStateException();
        }

    }

    private static class ReaderAccessor<T> {

        private final Object delegate;

        public ReaderAccessor(Object delegate) {
            this.delegate = delegate;
        }

        @SuppressWarnings("unchecked")
        public T get(Object target) throws IllegalAccessException, InvocationTargetException {
            if (delegate instanceof Field) {
                return (T) ((Field) delegate).get(target);
            } else if (delegate instanceof Method) {
                final Method method = (Method) delegate;
                return (T) method.invoke(target);
            }
            throw new IllegalStateException();
        }

        @SuppressWarnings("unchecked")
        public Class<T> getType() {
            if (delegate instanceof Field) {
                return (Class<T>) ((Field) delegate).getType();
            } else if (delegate instanceof Method) {
                return (Class<T>) ((Method) delegate).getReturnType();
            }
            throw new IllegalStateException();
        }

    }

    private static class WriterAccessor<T> {

        private final Object delegate;

        public WriterAccessor(Object delegate) {
            this.delegate = delegate;
        }

        public void set(Object target, T value) throws IllegalAccessException, InvocationTargetException {
            if (delegate instanceof Field) {
                ((Field) delegate).set(target, value);
            } else if (delegate instanceof Method) {
                ((Method) delegate).invoke(target, value);
            }
            throw new IllegalStateException();
        }

    }

    private static class Accessor<T> extends Annotated {

        private final ReaderAccessor<T> reader;
        private final WriterAccessor<T> writer;

        public Accessor(Object reader, Object writer) {
            super(reader);
            this.reader = new ReaderAccessor<T>(reader);
            this.writer = new WriterAccessor<T>(writer);
        }

        public T get(Object target) throws InvocationTargetException, IllegalAccessException {
            return reader.get(target);
        }

        public void set(Object target, T value) throws InvocationTargetException, IllegalAccessException {
            writer.set(target, value);
        }

        public Class<T> getType() {
            return reader.getType();
        }

    }

}
