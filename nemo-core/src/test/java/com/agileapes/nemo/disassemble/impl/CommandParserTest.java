package com.agileapes.nemo.disassemble.impl;

import com.agileapes.nemo.error.CommandSyntaxError;
import com.agileapes.nemo.option.OptionDescriptor;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/18/13, 10:43 AM)
 */
public class CommandParserTest {

    private List<OptionDescriptor> parse(String command) throws CommandSyntaxError {
        return new CommandParser(command).getOptions();
    }

    @Test
    public void testEmptyString() throws Exception {
        final List<OptionDescriptor> options = parse("action");
        Assert.assertTrue(options.isEmpty());
    }

    @Test
    public void testIndexed() throws Exception {
        final List<OptionDescriptor> descriptors = parse("action name");
        Assert.assertFalse(descriptors.isEmpty());
        Assert.assertEquals(descriptors.size(), 1);
        final OptionDescriptor descriptor = descriptors.iterator().next();
        Assert.assertNotNull(descriptor);
        Assert.assertFalse(descriptor.hasAlias());
        Assert.assertTrue(descriptor.hasIndex());
        Assert.assertEquals(descriptor.getName(), "name");
        Assert.assertNull(descriptor.getAlias());
        Assert.assertNotNull(descriptor.getIndex());
        Assert.assertEquals(descriptor.getIndex(), (Integer) 0);
        Assert.assertTrue(descriptor.isRequired());
    }

    @Test
    public void testNamed() throws Exception {
        final List<OptionDescriptor> descriptors = parse("action --name");
        Assert.assertFalse(descriptors.isEmpty());
        Assert.assertEquals(descriptors.size(), 1);
        final OptionDescriptor descriptor = descriptors.iterator().next();
        Assert.assertNotNull(descriptor);
        Assert.assertFalse(descriptor.hasAlias());
        Assert.assertFalse(descriptor.hasIndex());
        Assert.assertEquals(descriptor.getName(), "name");
        Assert.assertNull(descriptor.getAlias());
        Assert.assertNull(descriptor.getIndex());
        Assert.assertTrue(descriptor.isRequired());
    }

    @Test
    public void testAliased() throws Exception {
        final List<OptionDescriptor> descriptors = parse("action --n|name");
        Assert.assertFalse(descriptors.isEmpty());
        Assert.assertEquals(descriptors.size(), 1);
        final OptionDescriptor descriptor = descriptors.iterator().next();
        Assert.assertNotNull(descriptor);
        Assert.assertTrue(descriptor.hasAlias());
        Assert.assertFalse(descriptor.hasIndex());
        Assert.assertEquals(descriptor.getName(), "name");
        Assert.assertNotNull(descriptor.getAlias());
        Assert.assertNull(descriptor.getIndex());
        Assert.assertEquals(descriptor.getAlias(), (Object) 'n');
        Assert.assertTrue(descriptor.isRequired());
    }

    @Test
    public void testComplexCommand() throws Exception {
        final List<OptionDescriptor> descriptors = parse("action [--x|list --read] --options|o first [second]");
        Assert.assertNotNull(descriptors);
        Assert.assertFalse(descriptors.isEmpty());
        Assert.assertEquals(descriptors.size(), 5);
        //[--x|list]
        Assert.assertNotNull(descriptors.get(0));
        Assert.assertTrue(descriptors.get(0).hasAlias());
        Assert.assertFalse(descriptors.get(0).hasIndex());
        Assert.assertEquals(descriptors.get(0).getName(), "list");
        Assert.assertEquals(descriptors.get(0).getAlias(), (Object) 'x');
        Assert.assertFalse(descriptors.get(0).isRequired());
        //[--read]
        Assert.assertNotNull(descriptors.get(1));
        Assert.assertFalse(descriptors.get(1).hasAlias());
        Assert.assertFalse(descriptors.get(1).hasIndex());
        Assert.assertEquals(descriptors.get(1).getName(), "read");
        Assert.assertFalse(descriptors.get(1).isRequired());
        //--options|o
        Assert.assertNotNull(descriptors.get(2));
        Assert.assertTrue(descriptors.get(2).hasAlias());
        Assert.assertFalse(descriptors.get(2).hasIndex());
        Assert.assertEquals(descriptors.get(2).getName(), "options");
        Assert.assertEquals(descriptors.get(2).getAlias(), (Object) 'o');
        Assert.assertTrue(descriptors.get(2).isRequired());
        //first
        Assert.assertNotNull(descriptors.get(3));
        Assert.assertFalse(descriptors.get(3).hasAlias());
        Assert.assertTrue(descriptors.get(3).hasIndex());
        Assert.assertEquals(descriptors.get(3).getName(), "first");
        Assert.assertEquals(descriptors.get(3).getIndex(), (Object) 0);
        Assert.assertTrue(descriptors.get(3).isRequired());
        //[second]
        Assert.assertNotNull(descriptors.get(4));
        Assert.assertFalse(descriptors.get(4).hasAlias());
        Assert.assertTrue(descriptors.get(4).hasIndex());
        Assert.assertEquals(descriptors.get(4).getName(), "second");
        Assert.assertEquals(descriptors.get(4).getIndex(), (Object) 1);
        Assert.assertFalse(descriptors.get(4).isRequired());
    }

    @Test(expectedExceptions = CommandSyntaxError.class, expectedExceptionsMessageRegExp = ".*Missing.*]")
    public void testBracketLeftOpen() throws Exception {
        parse("action [test --name] [hello");
    }

    @Test(expectedExceptions = CommandSyntaxError.class, expectedExceptionsMessageRegExp = ".*Invalid.*]")
    public void testBracketNotOpened() throws Exception {
        parse("action test --name]");
    }

    @Test(expectedExceptions = CommandSyntaxError.class, expectedExceptionsMessageRegExp = ".*Invalid.*\\[")
    public void testBracketOpenedTwice() throws Exception {
        parse("action [test [--name]]");
    }

    @Test(expectedExceptions = CommandSyntaxError.class, expectedExceptionsMessageRegExp = ".*Expected option name missing")
    public void testEmptyOption() throws Exception {
        parse("action --");
    }

    @Test(expectedExceptions = CommandSyntaxError.class, expectedExceptionsMessageRegExp = ".*Options cannot have more than one alias")
    public void testMoreThanOneAlias() throws Exception {
        parse("action --a|b|c");
    }

    @Test(expectedExceptions = CommandSyntaxError.class, expectedExceptionsMessageRegExp = ".*at least two characters long")
    public void testNoNameForOption() throws Exception {
        parse("action --a");
    }

    @Test(expectedExceptions = CommandSyntaxError.class, expectedExceptionsMessageRegExp = ".*Option names and aliases must not be empty")
    public void testEmptyAlias() throws Exception {
        parse("action --|name");
    }

    @Test(expectedExceptions = CommandSyntaxError.class, expectedExceptionsMessageRegExp = ".*Option cannot be without a name")
    public void testTwoAliases() throws Exception {
        parse("action --a|b");
    }

}
