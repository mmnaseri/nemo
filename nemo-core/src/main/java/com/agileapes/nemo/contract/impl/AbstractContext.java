package com.agileapes.nemo.contract.impl;

import com.agileapes.nemo.contract.BeanProcessor;
import com.agileapes.nemo.contract.Context;
import com.agileapes.nemo.contract.Filter;
import com.agileapes.nemo.contract.OrderedBean;
import com.agileapes.nemo.error.DuplicateItemException;
import com.agileapes.nemo.error.InvalidItemNameException;
import com.agileapes.nemo.error.NoSuchItemException;
import com.agileapes.nemo.error.RegistryException;
import com.agileapes.nemo.util.CollectionDSL;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The abstract context implements all the basic functionalities expected from any context, leaving the map to the beans
 * to be defined by the extending class
 *
 * @see #getMap()
 * @see #namesAreTypeSpecific
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 16:44)
 */
public abstract class AbstractContext<C> implements Context<C> {

    /**
     * @return the map which will contain the registry of the beans
     */
    protected abstract Map<String, C> getMap();

    protected abstract Class<C> getType();

    /**
     * This parameter, if set to {@link true} will enforce a policy for registration of beans that will require all
     * bean names to be equal to the canonical names of their defining classes
     */
    protected boolean namesAreTypeSpecific = false;
    private List<BeanProcessor> beanProcessors = new CopyOnWriteArrayList<BeanProcessor>();

    @Override
    public void register(String name, C item) throws RegistryException {
        if (getMap().containsKey(name)) {
            throw new DuplicateItemException(name);
        }
        if (namesAreTypeSpecific && !item.getClass().getCanonicalName().equals(name)) {
            throw new InvalidItemNameException(name, item.getClass().getCanonicalName());
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
        return postProcessBeforeDispense(name, getMap().get(name));
    }

    @Override
    public C[] find(Filter<C> filter) throws RegistryException {
        if (getMap().isEmpty()) {
            throw new NoSuchItemException(null);
        }
        final Set<C> set = new HashSet<C>();
        for (Map.Entry<String, C> entry : getMap().entrySet()) {
            if (filter.accepts(entry.getValue())) {
                set.add(postProcessBeforeDispense(entry.getKey(), entry.getValue()));
            }
        }
        //noinspection unchecked
        return set.toArray((C[]) Array.newInstance(getType(), set.size()));
    }

    @SuppressWarnings("unchecked")
    private C postProcessBeforeRegister(String name, C item) throws RegistryException {
        C bean = item;
        for (BeanProcessor processor : beanProcessors) {
            Object changed = processor.postProcessBeforeRegistration(bean, name);
            if (changed == null) {
                changed = bean;
            }
            bean = (C) changed;
        }
        return bean;
    }

    @SuppressWarnings("unchecked")
    private C postProcessBeforeDispense(String name, C item) throws RegistryException {
        C bean = item;
        for (BeanProcessor processor : beanProcessors) {
            Object changed = processor.postProcessBeforeDispense(bean, name);
            if (changed == null) {
                changed = bean;
            }
            bean = (C) changed;
        }
        return bean;
    }

    @Override
    public void addBeanProcessor(BeanProcessor beanProcessor) {
        beanProcessors.add(beanProcessor);
        beanProcessors = CollectionDSL.sorted(beanProcessors, new Comparator<BeanProcessor>() {
            @Override
            public int compare(BeanProcessor o1, BeanProcessor o2) {
                final Integer first = o1 instanceof OrderedBean ? ((OrderedBean) o1).getOrder() : 0;
                final Integer second = o2 instanceof OrderedBean ? ((OrderedBean) o2).getOrder() : 0;
                return first.compareTo(second);
            }
        });
    }

    public List<BeanProcessor> getBeanProcessors() {
        return beanProcessors;
    }
}
