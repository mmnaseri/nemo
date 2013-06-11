package com.agileapes.nemo.disassemble.impl;

import com.agileapes.nemo.contract.Cache;
import com.agileapes.nemo.disassemble.DisassembleStrategy;
import com.agileapes.nemo.error.InvalidArgumentSyntaxException;
import com.agileapes.nemo.error.NoSuchOptionException;
import com.agileapes.nemo.error.OptionDefinitionException;
import com.agileapes.nemo.error.WrappedError;
import com.agileapes.nemo.option.OptionDescriptor;
import com.agileapes.nemo.value.ValueReader;
import com.agileapes.nemo.value.ValueReaderAware;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 18:27)
 */
public abstract class AbstractCachingDisassembleStrategy<A, D extends OptionDescriptor> implements DisassembleStrategy<A>, Cache<A, Set<D>>, ValueReaderAware {

    private final Map<A, Set<D>> cache = new ConcurrentHashMap<A, Set<D>>();
    private ValueReader valueReader;

    protected abstract Set<D> describe(A action) throws OptionDefinitionException;

    protected abstract void setOption(A action, D target, Object converted);

    @Override
    public void setValueReader(ValueReader valueReader) {
        this.valueReader = valueReader;
    }

    @Override
    public void write(A key, Set<D> value) {
        if (cache.containsKey(key)) {
            cache.remove(key);
        }
        cache.put(key, value);
    }

    @Override
    public Set<D> read(A key) {
        if (has(key)) {
            return cache.get(key);
        }
        Set<D> described = null;
        try {
            described = describe(key);
        } catch (OptionDefinitionException e) {
            throw new WrappedError(e);
        }
        if (!(described instanceof CopyOnWriteArrayList)) {
            described = new CopyOnWriteArraySet<D>(described);
        }
        write(key, described);
        return described;
    }

    public Set<D> readItem(A key) throws OptionDefinitionException {
        try {
            return read(key);
        } catch (WrappedError e) {
            final OptionDefinitionException error = e.getWrappedError(OptionDefinitionException.class);
            throw error;
        }
    }

    @Override
    public boolean has(A key) {
        return cache.containsKey(key);
    }

    @Override
    public OptionDescriptor getOption(A action, String option) throws NoSuchOptionException, OptionDefinitionException {
        final Set<D> descriptors = readItem(action);
        for (OptionDescriptor descriptor : descriptors) {
            if (descriptor.getName().equals(option)) {
                return descriptor;
            }
        }
        throw new NoSuchOptionException(option);
    }

    @Override
    public OptionDescriptor getOption(A action, Character alias) throws NoSuchOptionException, OptionDefinitionException {
        final Set<D> descriptors = readItem(action);
        for (OptionDescriptor descriptor : descriptors) {
            if (descriptor.hasAlias() && descriptor.getAlias().equals(alias)) {
                return descriptor;
            }
        }
        throw new NoSuchOptionException(alias);
    }

    @Override
    public OptionDescriptor getOption(A action, Integer index) throws NoSuchOptionException, OptionDefinitionException {
        final Set<D> descriptors = readItem(action);
        for (OptionDescriptor descriptor : descriptors) {
            if (descriptor.hasIndex() && descriptor.getIndex().equals(index)) {
                return descriptor;
            }
        }
        throw new NoSuchOptionException(index);
    }

    @Override
    public Set<D> getOptions(A action) throws OptionDefinitionException {
        return readItem(action);
    }

    @Override
    public void setOption(A action, OptionDescriptor descriptor, String value) throws NoSuchOptionException, OptionDefinitionException {
        final Set<D> descriptors = readItem(action);
        D target = null;
        for (D optionDescriptor : descriptors) {
            if (optionDescriptor.getName().equals(descriptor.getName())) {
                target = optionDescriptor;
                break;
            }
        }
        if (target == null) {
            throw new NoSuchOptionException(descriptor.getName());
        }
        final Object converted;
        try {
            converted = valueReader.read(value, target.getType());
        } catch (Throwable e) {
            throw new InvalidArgumentSyntaxException(target.getName(), value);
        }
        setOption(action, target, converted);
    }

    @Override
    public void reset(A action) throws OptionDefinitionException {
        final Set<D> options = readItem(action);
        for (D option : options) {
            setOption(action, option, option.getDefaultValue());
        }
    }

    @Override
    public boolean accepts(Object action) {
        try {
            //noinspection unchecked
            final A cast = (A) action;
        } catch (ClassCastException e) {
            return false;
        }
        return true;
    }

}
