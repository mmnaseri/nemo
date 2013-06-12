package com.agileapes.nemo.disassemble;

/**
 * This interface should be implemented by actions that are aware of their disassemblers at runtime.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 17:55)
 */
public interface DisassemblerAware<A> {

    /**
     * @return the disassembler for this action
     */
    DisassembleStrategy<A> getDisassembler();

}
