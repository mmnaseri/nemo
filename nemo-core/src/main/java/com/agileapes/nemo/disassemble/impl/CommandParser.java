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
