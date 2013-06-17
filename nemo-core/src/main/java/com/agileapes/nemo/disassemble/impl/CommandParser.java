package com.agileapes.nemo.disassemble.impl;

import com.agileapes.nemo.api.Command;
import com.agileapes.nemo.error.CommandSyntaxError;
import com.agileapes.nemo.error.OptionDefinitionException;
import com.agileapes.nemo.option.OptionDescriptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is an asset for the {@link CommandStatementDisassembleStrategy} strategy. This parser is supposed to read
 * the definition of an action from the given @Command annotation (or as plain text, otherwise), and provide the
 * descriptions determined from this input.
 *
 * The input must follow these guidelines:
 *
 * <ul>
 *     <li>It can be the name of an option, in which case it will be assumed as an indexed option, whose
 *     corresponding field name has been given here. For instance, should the command description be
 *     {@code verbose}, it would mean that the action can be invoked by:
 *     {@code %APPLICATION% action <verbose>} or {@code %APPLICATION% action --verbose <value>}</li>
 *     <li>It can be the option value format for the given option. Aliases can be specified here, also.
 *     The syntax for this definition is: {@code --name|alias} where name must be longer than one character.
 *     Specifying an alias is not mandatory and the order in which aliases and names are specified is not
 *     obligatory either. Should you enter {@code --verbose|v} it would mean that this action can be invoked
 *     in one of these ways:
 *     {@code %APPLICATION% action --verbose <value>} or {@code %APPLICATION% action -v <value>}</li>
 *     <li>Optional portions of the definition must be encased in square brackets.</li>
 * </ul>
 *
 * To exemplify, let us assume that we want to allow users to invoke the <strong>read</strong> action
 * which takes in a file name, and can optionally display line numbers.
 *
 * The syntax for such a command would be:
 *
 * <code>file [--n|numbers]</code>
 *
 * Which would indicate that the line number property is optional, while the file option is mandatory
 * and can be called with its index.
 *
 * To see how option types are determined and how their values are accessed, please refer to
 * {@link CommandStatementDisassembleStrategy}
 *
 * @see CommandStatementDisassembleStrategy
 * @see Command
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/15/13, 6:14 PM)
 */
public class CommandParser {

    private List<OptionDescriptor> options = new ArrayList<OptionDescriptor>();
    private String command;
    private int position;

    public CommandParser(Command command) throws CommandSyntaxError {
        this(command.value());
    }

    public CommandParser(String command) throws CommandSyntaxError {
        this.command = command;
        parse();
    }

    public List<OptionDescriptor> getOptions() {
        return options;
    }

    private void parse() throws CommandSyntaxError {
        position = 0;
        skip();
        boolean optional = false;
        String name;
        Character alias;
        Integer index = 0;
        while (position < command.length()) {
            if (command.charAt(position) == '[') {
                if (optional) {
                    throw new CommandSyntaxError(position, "Invalid token [");
                } else {
                    optional = true;
                }
                position ++;
                skip();
            } else if (command.charAt(position) == ']') {
                if (optional) {
                    optional = false;
                } else {
                    throw new CommandSyntaxError(position, "Invalid token ]");
                }
                position ++;
                skip();
            } else if (command.substring(position).startsWith("--")) {
                position += 2;
                final String[] split = read().split("\\|");
                if (split.length == 0) {
                    throw new CommandSyntaxError(position, "Expected option name missing");
                }
                if (split.length > 2) {
                    throw new CommandSyntaxError(position, "Options cannot have more than one alias");
                }
                if (split.length == 1) {
                    if (split[0].length() < 2) {
                        throw new CommandSyntaxError(position, "Option names must be at least two characters long");
                    }
                    alias = null;
                    name = split[0];
                } else {
                    if (split[0].isEmpty() || split[1].isEmpty()) {
                        throw new CommandSyntaxError(position, "Option names and aliases must not be empty");
                    }
                    if (split[0].length() != 1 && split[1].length() != 1) {
                        throw new CommandSyntaxError(position, "Option aliases must be a single character");
                    }
                    if (split[0].length() == 1 && split[1].length() == 1) {
                        throw new CommandSyntaxError(position, "Option aliases must be a single character");
                    }
                    if (split[0].length() == 1) {
                        alias = split[0].charAt(0);
                        name = split[1];
                    } else {
                        alias = split[1].charAt(0);
                        name = split[0];
                    }
                }
                skip();
                try {
                    options.add(new OptionDescriptor(name, alias, null, !optional, null, null, null));
                } catch (OptionDefinitionException ignored) {
                }
            } else {
                name = read();
                skip();
                try {
                    options.add(new OptionDescriptor(name, null, index ++, !optional, null, null, null));
                } catch (OptionDefinitionException ignored) {
                }
            }
        }
        if (optional) {
            throw new CommandSyntaxError(position, "Missing token ]");
        }
        if (position < command.length()) {
            throw new CommandSyntaxError(position, "Invalid input");
        }
        final Set<String> properties = new HashSet<String>();
        for (OptionDescriptor option : getOptions()) {
            if (properties.contains(option.getName())) {
                throw new CommandSyntaxError(0, "Duplicate option: " + option.getName());
            }
            properties.add(option.getName());
        }
    }

    public void skip() {
        while (position < command.length() && Character.isWhitespace(command.charAt(position))) {
            position ++;
        }
    }

    public String read() {
        String value = "";
        while (position < command.length() && !Character.isWhitespace(command.charAt(position))
                && !"-[]".contains(String.valueOf(command.charAt(position)))) {
            value += command.charAt(position);
            position ++;
        }
        return value;
    }

}
