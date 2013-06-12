package com.agileapes.nemo.option;

import com.agileapes.nemo.error.OptionDefinitionException;

/**
 * This class encapsulates the definition of an option as seen by the framework, through what the action
 * developer has made available.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 17:20)
 */
public class OptionDescriptor {

    private final String name;
    private final Character alias;
    private final Integer index;
    private final boolean required;
    private final Class<?> type;
    private final Object defaultValue;

    public OptionDescriptor(String name, Character alias, Integer index, boolean required, Class<?> type, Object defaultValue) throws OptionDefinitionException {
        if (name == null) {
            throw new OptionDefinitionException("Option name cannot be null");
        }
        this.name = name;
        this.alias = alias;
        this.index = index;
        this.required = required;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public Character getAlias() {
        return alias;
    }

    public Integer getIndex() {
        return index;
    }

    public boolean isRequired() {
        return required;
    }

    public Class<?> getType() {
        return type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public boolean hasAlias() {
        return alias != null;
    }

    public boolean hasIndex() {
        return index != null;
    }

}
