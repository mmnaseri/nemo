package com.agileapes.nemo.demo;

import com.agileapes.nemo.contract.Executable;
import com.agileapes.nemo.value.ValueReader;
import com.agileapes.nemo.value.ValueReaderAware;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/12, 3:44)
 */
public class ValueUser implements ValueReaderAware, Executable {

    private ValueReader valueReader;

    @Override
    public void setValueReader(ValueReader valueReader) {
        this.valueReader = valueReader;
    }

    @Override
    public void execute() throws Exception {
        System.out.println(valueReader.read("12.1", Double.class));
    }

}
