package com.agileapes.nemo.disassemble.impl;

import com.agileapes.couteau.basics.api.Processor;
import com.agileapes.couteau.reflection.util.assets.AnnotatedElementFilter;
import com.agileapes.couteau.reflection.util.assets.MemberModifierFilter;
import com.agileapes.couteau.reflection.util.assets.Modifiers;
import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.api.Command;
import com.agileapes.nemo.api.Disassembler;
import com.agileapes.nemo.api.Option;
import com.agileapes.nemo.contract.Executable;
import com.agileapes.nemo.error.OptionDefinitionException;
import com.agileapes.nemo.option.OptionDescriptor;
import com.agileapes.nemo.util.AnnotationPropertyBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static com.agileapes.couteau.basics.collections.CollectionWrapper.with;
import static com.agileapes.couteau.reflection.util.ReflectionUtils.withFields;

/**
 * This strategy relies on the {@link Option} annotation being present on the fields of the action class, and the action
 * class being a descendant of {@link Action}. This strategy will then easily work through reflection to manage the task
 * of discerning options
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 17:59)
 */
public class AnnotatedFieldsDisassembleStrategy extends AbstractCachingDisassembleStrategy<Action, AnnotatedFieldsDisassembleStrategy.FieldOptionDescriptor> {

    private static final Log log = LogFactory.getLog(AnnotatedFieldsDisassembleStrategy.class);

    @Override
    protected Set<FieldOptionDescriptor> describe(final Action action) throws OptionDefinitionException {
        log.info("Attempting to extrapolate options for action: " + action);
        final HashSet<FieldOptionDescriptor> descriptors = new HashSet<FieldOptionDescriptor>();
        log.info("Scanning fields for annotation @Option");
        try {
            //noinspection unchecked
            withFields(action.getClass())
                    .keep(new AnnotatedElementFilter(Option.class))
                    .drop(new MemberModifierFilter(Modifiers.STATIC))
                    .each(new Processor<Field>() {
                        @Override
                        public void process(Field field) throws Exception {
                            field.setAccessible(true);
                            final String propertyName = field.getName();
                            log.info("Extracting metadata for field " + propertyName);
                            final Option annotation = field.getAnnotation(Option.class);
                            final Properties properties = new Properties();
                            try {
                                with(field.getAnnotations()).each(new Processor<Annotation>() {
                                    @Override
                                    public void process(Annotation item) throws Exception {
                                        new AnnotationPropertyBuilder(item).addTo(properties);
                                    }
                                });
                            } catch (Exception e) {
                                throw new OptionDefinitionException("Could not get a description for option", e);
                            }
                            descriptors.add(new FieldOptionDescriptor(propertyName, annotation.alias() != ' ' ? annotation.alias() : null, annotation.index() >= 0 ? annotation.index() : null, annotation.required(), field.getType(), field.get(action), field, properties));
                        }
                    });
        } catch (Exception ignored) {
            if (ignored instanceof OptionDefinitionException) {
                throw (OptionDefinitionException) ignored;
            }
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
        return action instanceof Action &&
                (!action.getClass().isAnnotationPresent(Disassembler.class) || action.getClass().getAnnotation(Disassembler.class).value().equals(getClass())) &&
                (!action.getClass().isAnnotationPresent(Command.class));
    }

    @Override
    public Properties getMetadata(Action action) {
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

    public final static class FieldOptionDescriptor extends OptionDescriptor {

        private final Field field;

        public FieldOptionDescriptor(String name, Character alias, Integer index, boolean required, Class<?> type, Object defaultValue, Field field, Properties properties) throws OptionDefinitionException {
            super(name, alias, index, required, type, defaultValue, properties);
            this.field = field;
        }

        public Field getField() {
            return field;
        }

    }

}
