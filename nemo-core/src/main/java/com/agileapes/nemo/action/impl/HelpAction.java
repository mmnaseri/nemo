package com.agileapes.nemo.action.impl;

import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.action.ActionContextAware;
import com.agileapes.nemo.api.Disassembler;
import com.agileapes.nemo.api.Help;
import com.agileapes.nemo.api.Option;
import com.agileapes.nemo.disassemble.impl.AnnotatedFieldsDisassembleStrategy;
import com.agileapes.nemo.error.NoSuchOptionException;
import com.agileapes.nemo.option.OptionDescriptor;
import com.agileapes.nemo.util.CollectionDSL;
import com.agileapes.nemo.util.ReflectionUtils;

import java.util.*;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/13/13, 11:58 AM)
 */
@Help(
        value = "Provides help for the application as a whole, and specific targets if need be",
        description = "To get help for a specific target use '--target', and to see option helps " +
                "use '--option'.\n" +
                "Setting '--option' or '--target' to '*' is the same as leaving them " +
                "unset, which means you do not care for a specific target or option."
)
@Disassembler(AnnotatedFieldsDisassembleStrategy.class)
public class HelpAction extends Action implements ActionContextAware {

    private static final String ALL = "*";
    public static final String HELP = "@" + Help.class.getCanonicalName();
    private ActionContext actionContext;

    @Option(index = 0)
    @Help("Determines which target you want help with")
    private String target = ALL;

    @Option(index = 1)
    @Help("Determines which options you want help with")
    private String option = ALL;

    @Override
    public void setActionContext(ActionContext actionContext) {
        this.actionContext = actionContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws Exception {
        if (target == null || target.isEmpty()) {
            target = ALL;
        }
        if (option == null || option.isEmpty()) {
            option = ALL;
        }
        if (ALL.equals(target) && ALL.equals(option)) {
            //we need help for all the available actions
            final List<String> names = CollectionDSL.sorted(actionContext.getActions().keySet());
            output.println("Available actions:");
            output.println("------------------");
            int length = 0;
            for (String name : names) {
                final SmartAction action = (SmartAction) actionContext.get(name);
                if (action.isInternal()) {
                    continue;
                }
                length = Math.max(length, name.length() + 2);
            }
            for (String name : names) {
                final SmartAction action = (SmartAction) actionContext.get(name);
                if (action.isInternal()) {
                    continue;
                }
                if (action.isDefaultAction()) {
                    output.print(" * ");
                } else {
                    output.print("   ");
                }
                output.print(name);
                final Properties metadata = action.getMetadata();
                if (metadata.containsKey(HELP)) {
                    for (int i = 0; i < length - name.length(); i ++) {
                        output.print(" ");
                    }
                    int indent = length + 3;
                    String message = (String) ((Map<String, Object>) metadata.get(HELP)).get("value");
                    message = message.trim();
                    while (message.length() > 65) {
                        int i = 65;
                        while (!Character.isWhitespace(message.charAt(i))) {
                            i --;
                        }
                        output.print(message.substring(0, i));
                        message = message.substring(i).trim();
                        if (!message.isEmpty()) {
                            output.println();
                            for (int j = 0; j < indent; j ++) {
                                output.print(" ");
                            }
                        }
                    }
                    output.print(message);
                }
                output.println();
            }
        } else if (ALL.equals(target) && !ALL.equals(option)) {
            //we want help for an option of the default action
            target = actionContext.getDefaultAction().getName();
            execute();
        } else if (ALL.equals(option)) {
            //we want help for a all of the options of a specific target
            final SmartAction action = (SmartAction) actionContext.get(target);
            output.println("Action: " + target);
            printHelp(action.getMetadata());
            final List<OptionDescriptor> options = CollectionDSL.sorted((Set<OptionDescriptor>) action.getOptions(), new Comparator<OptionDescriptor>() {
                @Override
                public int compare(OptionDescriptor o1, OptionDescriptor o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            if (!options.isEmpty()) {
                output.println("Available options: ");
                for (OptionDescriptor descriptor : options) {
                    output.print("\t--" + descriptor.getName());
                    if (descriptor.hasAlias()) {
                        output.print(" (or -" + descriptor.getAlias() + ")");
                    }
                    if (!descriptor.isRequired()) {
                        output.print(" (optional)");
                    }
                    output.println();
                }
            }
        } else {
            //we want help for a specific option under a specific target
            final SmartAction action = (SmartAction) actionContext.get(target);
            OptionDescriptor descriptor = null;
            try {
                descriptor = action.getDisassembler().getOption(action.getAction(), option);
            } catch (NoSuchOptionException ignored) {
            }
            if (descriptor == null && option.length() == 1) {
                descriptor = action.getDisassembler().getOption(action.getAction(), option.charAt(0));
            }
            if (descriptor == null) {
                throw new NoSuchOptionException(option);
            }
            output.print("Usage: " + target + " --" + descriptor.getName());
            if (!descriptor.isFlag()) {
                output.print(" <value>");
            }
            output.println();
            if (descriptor.hasAlias()) {
                output.print("   or: " + target + " -" + descriptor.getAlias());
                if (!descriptor.isFlag()) {
                    output.print(" <value>");
                }
                output.println();
            }
            if (descriptor.isFlag()) {
                output.println("This option is a flag. This means that by including it you are setting its value to `true`.");
                output.print("However, you can set its value to `false` by writing `" + target + " --" + descriptor.getName() + " false`");
                if (descriptor.hasAlias()) {
                    output.print(" or `" + target + " -" + descriptor.getAlias() + " false`");
                }
                output.println();
            } else {
                output.println("Here value is " + ReflectionUtils.describeType(descriptor.getType()));
            }
            if (!descriptor.isRequired()) {
                output.println("Setting this option is not mandatory.");
                output.println("The default value for this option " +
                        "is: " + descriptor.getDefaultValue());
            }
            printHelp(descriptor.getMetadata());
        }
    }

    private void printHelp(Properties metadata) {
        output.println();
        if (metadata.containsKey(HELP)) {
            //noinspection unchecked
            final Map<String, Object> map = (Map<String, Object>) metadata.get(HELP);
            output.print(((String) map.get("value")).isEmpty() ? "" : map.get("value") + "\n");
            output.print(((String) map.get("description")).isEmpty() ? "" : map.get("description") + "\n");
        } else {
            output.println("No further help has been provided by the developer.");
        }
    }

}
