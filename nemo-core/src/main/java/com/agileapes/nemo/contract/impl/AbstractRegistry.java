package com.agileapes.nemo.contract.impl;

import com.agileapes.nemo.contract.Filter;
import com.agileapes.nemo.contract.Registry;
import com.agileapes.nemo.error.DuplicateItemException;
import com.agileapes.nemo.error.NoSuchItemException;
import com.agileapes.nemo.error.RegistryException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 16:44)
 */
public abstract class AbstractRegistry<C> implements Registry<C> {

    protected abstract Map<String, C> getMap();

    @Override
    public void register(String name, C item) throws RegistryException {
        if (getMap().containsKey(name)) {
            throw new DuplicateItemException(name);
        }
        C changed = postProcessBeforeRegister(name, item);
        if (changed == null) {
            changed = item;
        }
        getMap().put(name, changed);
    }

    @Override
    public C get(String name) throws RegistryException {
        if (!getMap().containsKey(name)) {
            throw new NoSuchItemException(name);
        }
        C item = getMap().get(name);
        C changed = postProcessBeforeDispense(name, item);
        if (changed == null) {
            changed = item;
        }
        return changed;
    }

    @Override
    public C[] find(Filter<C> filter) throws RegistryException {
        final Set<C> set = new HashSet<C>();
        for (Map.Entry<String, C> entry : getMap().entrySet()) {
            if (filter.accepts(entry.getValue())) {
                C item = postProcessBeforeDispense(entry.getKey(), entry.getValue());
                if (item == null) {
                    item = entry.getValue();
                }
                set.add(item);
            }
        }
        //noinspection unchecked
        return (C[]) set.toArray();
    }

    protected C postProcessBeforeRegister(String name, C item) throws RegistryException {
        return null;
    }

    protected C postProcessBeforeDispense(String name, C item) throws RegistryException {
        return null;
    }
    
}
