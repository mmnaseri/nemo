package com.agileapes.nemo.disassemble.impl;

import com.agileapes.couteau.basics.api.Processor;
import com.agileapes.couteau.basics.collections.CollectionWrapper;
import com.agileapes.couteau.reflection.util.assets.*;
import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.api.Command;
import com.agileapes.nemo.api.Disassembler;
import com.agileapes.nemo.contract.Executable;
import com.agileapes.nemo.error.CommandSyntaxError;
import com.agileapes.nemo.error.OptionDefinitionException;
import com.agileapes.nemo.error.WrappedError;
import com.agileapes.nemo.option.OptionDescriptor;
import com.agileapes.nemo.util.AnnotationPropertyBuilder;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.agileapes.couteau.basics.collections.CollectionWrapper.with;
import static com.agileapes.couteau.reflection.util.ReflectionUtils.withFields;
import static com.agileapes.couteau.reflection.util.ReflectionUtils.withMethods;

/**
 * This strategy will look at all actions which are annotated with {@link Command}. The @Command
 * annotation is essentially an easier way for allowing developers to
 * indicate the options for each action. The syntax of the command is explained in the documentation for
 * {@link CommandParser}.
 *
 * Whenever an option is being described by this strategy, it first looks for accessor methods throughout
 * the hierarchy for that option. This means that if there is an option with the name 'verbose', to read its
 * value, the strategy will first look up method 'getVerbose' in a place in the hierarchy and, should it fail
 * to locate such a method, it will proceed to look for a field with name 'verbose'. This is to honor encapsulation
 * as devised by Java language designers.
 *
 * The same process is repeated for writing to the property, only this time a setter method is looked up.
 *
 * Do note that this means accessor methods always have a priority over fields, regardless of the depths to which
 * we would have to travel to capture them.
 *
 * A common pitfall is for you to name a property which has a getter method higher
 * along the hierarchy, e.g. {@link Action#getName()}, which means that should you forget to provide your own
 * getter and setter for this property, the one from the {@link Action} will be used, which might not be
 * what you intended originally.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/15/13, 4:53 PM)
 */
public class CommandStatementDisassembleStrategy extends AbstractCachingDisassembleStrategy<Object, CommandStatementDisassembleStrategy.AccessibleFieldOptionDescriptor> {

    private Object defaultAction = null;
    private Set<Object> internalActions = new CopyOnWriteArraySet<Object>();

    @Override
    protected Set<AccessibleFieldOptionDescriptor> describe(Object action) throws OptionDefinitionException {
        final HashSet<AccessibleFieldOptionDescriptor> descriptors = new HashSet<AccessibleFieldOptionDescriptor>();
        final List<OptionDescriptor> options;
        try {
            final CommandParser parser = new CommandParser(action.getClass().getAnnotation(Command.class));
            if (parser.isDefaultAction()) {
                if (defaultAction != null) {
                    throw new OptionDefinitionException("Cannot have more than one default action");
                }
                defaultAction = action;
            } else if (parser.isInternal()) {
                internalActions.add(action);
            }
            options = parser.getOptions();
        } catch (CommandSyntaxError e) {
            throw new OptionDefinitionException("Could not parse command definition", e);
        }
        for (OptionDescriptor option : options) {
            final Accessor<?> accessor = getAccessor(action.getClass(), option.getName());
            final Properties properties = new Properties();
            try {
                with(accessor.getAnnotations()).each(new Processor<Annotation>() {
                    @Override
                    public void process(Annotation item) {
                        new AnnotationPropertyBuilder(item).addTo(properties);
                    }
                });
            } catch (Exception ignored) {
            }
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
        final Object[] fields;
        final Object[] getters;
        final Object[] setters;
        try {
            fields = withFields(type).drop(new MemberModifierFilter(Modifiers.STATIC)).keep(new MemberNameFilter(property)).list().toArray();
            getters = withMethods(type).keep(new GetterMethodFilter()).keep(new PropertyAccessorFilter(property)).list().toArray();
            setters = withMethods(type).keep(new SetterMethodFilter()).keep(new PropertyAccessorFilter(property)).list().toArray();
        } catch (Exception e) {
            return null;
        }
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
    protected void setOption(Object action, AccessibleFieldOptionDescriptor target, Object converted) {
        try {
            //noinspection unchecked
            target.getAccessor().set(action, converted);
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean isDefaultAction(Object action) {
        return action.equals(defaultAction);
    }

    @Override
    public boolean isInternal(Object action) {
        return internalActions.contains(action);
    }

    @Override
    public void setOutput(final Object action, final PrintStream output) {
        CollectionWrapper<Field> fields = null;
        try {
            fields = withFields(action.getClass()).keep(new FieldTypeFilter(PrintStream.class));
            if (fields.count() > 1) {
                fields = fields.keep(new MemberNameFilter("output"));
            }
        } catch (Exception ignored) {
        }
        assert fields != null;
        if (fields.count() > 0) {
            final Field field = fields.first();
            try {
                field.setAccessible(true);
                field.set(action, output);
            } catch (IllegalAccessException ignored) {
            }
        } else {
            try {
                withMethods(action.getClass())
                        .keep(new SetterMethodFilter())
                        .keep(new MethodArgumentsFilter(PrintStream.class))
                        .keep(new PropertyAccessorFilter("output"))
                        .each(new Processor<Method>() {
                            @Override
                            public void process(Method item) {
                                try {
                                    item.invoke(action, output);
                                } catch (Exception e) {
                                    throw new WrappedError(e);
                                }
                            }
                        });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Executable getExecutable(final Object action) {
        CollectionWrapper<Method> wrapper;
        try {
            wrapper = withMethods(action.getClass())
                    .keep(new MemberNameFilter("execute"))
                    .keep(new MethodReturnTypeFilter(void.class))
                    .keep(new MethodArgumentsFilter());
        } catch (Exception e) {
            wrapper = with(new Method[0]);
        }
        if (wrapper.isEmpty()) {
            throw new RuntimeException("Action has no executable method");
        }
        final CollectionWrapper<Method> finalWrapper = wrapper;
        return new Executable() {
            @Override
            public void execute() throws Exception {
                finalWrapper.first().invoke(action);
            }
        };
    }

    @Override
    public Properties getMetadata(Object action) {
        final Properties properties = new Properties();
        try {
            with(action.getClass().getAnnotations())
                    .each(new Processor<Annotation>() {
                        @Override
                        public void process(Annotation item) {
                            new AnnotationPropertyBuilder(item).addTo(properties);
                        }
                    });
        } catch (Exception ignored) {
        }
        return properties;
    }

    @Override
    public boolean accepts(Object action) {
        return action.getClass().isAnnotationPresent(Command.class) &&
                (!action.getClass().isAnnotationPresent(Disassembler.class) || action.getClass().getAnnotation(Disassembler.class).value().equals(CommandStatementDisassembleStrategy.class));
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
                final Field field = (Field) delegate;
                field.setAccessible(true);
                return (T) field.get(target);
            } else if (delegate instanceof Method) {
                final Method method = (Method) delegate;
                method.setAccessible(true);
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
