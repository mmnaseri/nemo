package com.agileapes.nemo.disassemble.impl;

import com.agileapes.nemo.contract.Cache;
import com.agileapes.nemo.disassemble.DisassembleStrategy;
import com.agileapes.nemo.option.OptionDescriptor;
import com.agileapes.nemo.value.ValueReaderContext;
import com.agileapes.nemo.value.ValueReaderContextAware;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 18:27)
 */
public abstract class AbstractCachingDisassembleStrategy<A, D extends OptionDescriptor> implements DisassembleStrategy<A>, Cache<A, Set<D>>, ValueReaderContextAware {

    private final Map<A, Set<D>> cache = new ConcurrentHashMap<A, Set<D>>();
    private ValueReaderContext valueReaderContext;

    protected abstract Set<D> describe(A action);

    protected abstract void setOption(A action, D target, Object converted);

    @Override
    public void setValueReaderContext(ValueReaderContext valueReaderContext) {
        this.valueReaderContext = valueReaderContext;
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
        Set<D> described = describe(key);
        if (!(described instanceof CopyOnWriteArrayList)) {
            described = new CopyOnWriteArraySet<D>(described);
        }
        write(key, described);
        return described;
    }

    @Override
    public boolean has(A key) {
        return cache.containsKey(key);
    }

    @Override
    public OptionDescriptor getOption(A action, String option) {
        final Set<D> descriptors = read(action);
        for (OptionDescriptor descriptor : descriptors) {
            if (descriptor.getName().equals(option)) {
                return descriptor;
            }
        }
        throw new IllegalArgumentException("No such option: --" + option);
    }

    @Override
    public OptionDescriptor getOption(A action, Character alias) {
        final Set<D> descriptors = read(action);
        for (OptionDescriptor descriptor : descriptors) {
            if (descriptor.hasAlias() && descriptor.getAlias().equals(alias)) {
                return descriptor;
            }
        }
        throw new IllegalArgumentException("No such option: -" + alias);
    }

    @Override
    public OptionDescriptor getOption(A action, Integer index) {
        final Set<D> descriptors = read(action);
        for (OptionDescriptor descriptor : descriptors) {
            if (descriptor.hasIndex() && descriptor.getIndex().equals(index)) {
                return descriptor;
            }
        }
        throw new IllegalArgumentException("No such option: %" + index);
    }

    @Override
    public Set<D> getOptions(A action) {
        return read(action);
    }

    @Override
    public void setOption(A action, OptionDescriptor descriptor, String value) {
        final Set<D> descriptors = read(action);
        D target = null;
        for (D optionDescriptor : descriptors) {
            if (optionDescriptor.getName().equals(descriptor.getName())) {
                target = optionDescriptor;
                break;
            }
        }
        if (target == null) {
            return;
        }
        final Object converted = valueReaderContext.read(value, target.getType());
        setOption(action, target, converted);
    }

    @Override
    public void reset(A action) {
        final Set<D> options = read(action);
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
