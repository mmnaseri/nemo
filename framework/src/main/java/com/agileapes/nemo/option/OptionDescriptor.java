/*
 * Copyright (c) 2013. AgileApes (http://www.agileapes.scom/), and
 * associated organization.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 */

package com.agileapes.nemo.option;

import java.util.Collections;
import java.util.Set;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/6, 16:18)
 */
public class OptionDescriptor implements Comparable<OptionDescriptor> {

    private final String name;
    private final Character alias;
    private final Integer index;
    private final boolean required;
    private Class<?> type;
    private final Set<Metadata> metadata;

    public OptionDescriptor(String name, Character alias, Integer index, boolean required, Class<?> type, Set<Metadata> metadata) {
        this.name = name;
        this.alias = alias;
        this.index = index;
        this.required = required;
        this.type = type;
        this.metadata = metadata;
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

    public Class<?> getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public Metadata getMetadata(String name) {
        for (Metadata item : metadata) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    public boolean hasMetadata(String name) {
        return getMetadata(name) != null;
    }

    public Set<Metadata> getAllMetadata() {
        return Collections.unmodifiableSet(metadata);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final OptionDescriptor that = (OptionDescriptor) o;
        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public int compareTo(OptionDescriptor o) {
        return getName().compareTo(o.getName());
    }

}
