package com.agileapes.nemo.error;

import com.agileapes.nemo.option.OptionDescriptor;

import java.util.Arrays;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 17:27)
 */
public class RequiredOptionsMissingException extends Exception {

    public RequiredOptionsMissingException(OptionDescriptor[] descriptors) {
        super("Values missing for required options: " + Arrays.toString(descriptors));
    }

}
