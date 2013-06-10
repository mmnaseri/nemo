package com.agileapes.nemo.disassemble;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 17:55)
 */
public interface DisassemblerAware<A> {

    DisassembleStrategy<A> getDisassembler();

}
