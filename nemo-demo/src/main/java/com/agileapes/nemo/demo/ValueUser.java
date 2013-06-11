package com.agileapes.nemo.demo;

import com.agileapes.nemo.contract.Executable;
import com.agileapes.nemo.value.ValueReaderContext;
import com.agileapes.nemo.value.ValueReaderContextAware;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/12, 3:44)
 */
public class ValueUser implements ValueReaderContextAware, Executable {

    private ValueReaderContext valueReaderContext;

    @Override
    public void setValueReaderContext(ValueReaderContext valueReaderContext) {
        this.valueReaderContext = valueReaderContext;
    }

    @Override
    public void execute() throws Exception {
        System.out.println(valueReaderContext.read("12.1", Double.class));
    }

}
