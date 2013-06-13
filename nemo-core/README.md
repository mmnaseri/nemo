# Under the Hood

In this section we will go over the inner workings of the core of **nemo** and see how it performs its actions. This will help you
understand how you can interact with the framework in the best possible manner.

## Actions

The actions can be defined in various ways, as is explained in *Strategies*, however, any action, regardless of how it is
defined will eventually end up as a descendant of `com.agileapes.nemo.action.Action`.

`Action` itself is an `Executable`, meaning that it can be executed and expected to thus carry out a single unit of work.

Actions must be registered with the `ActionContext` associated with the `ExecutorContext` in use.

## Strategies

As mentioned in the basics, extending the `Action` class is one way of defining actions. Actions can be defined through
various, custom ways. These ways are subject to the existence of strategies which can take care of extracting action
metadata and execution options from them. These are called `DisassembleStrategies`, or *disassemblers*, or *strategies*
for short. We will cover the strategies currently built into the system.

Strategies will be associated with the actions using the `@com.agileapes.nemo.api.Disassembler` annotation. If no such
annotation has been provided, the context will try the default strategy, and failing that, will look through the strategies
for a strategy that would accept the task of disassembling the given action.

### AnnotatedFieldsDisassembleStrategy

This strategy relies on the actions being descendants of `Action` itself. This means that it cannot take care of other
action types. Also, to extrapolate action options, it will look -- via reflection -- through all of the fields defined
in the action class (whether they are private is of no consequence) and selects those that are marked with `@Option`
available at `com.agileapes.nemo.api.Option`. It will then extract all the metadata required from the field.

You can find out the exact metadata this class extracts by taking a look at the option descriptor used which is available at
`com.agileapes.nemo.disassemble.impl.AnnotatedFieldsDisassembleStrategy.FieldOptionDescriptor`.

At the moment, **this is the default strategy**.

## Values

Values are converted to their actual type from their textual representations. This is done
automatically and via the `com.agileapes.nemo.value.ValueReader` interface.

Certain value readers are provided by default. The types recognized at the moment (other than
the seven standard primitive types) includes:

 * java.lang.String
 * java.lang.Integer
 * java.lang.Long
 * java.lang.Short
 * java.lang.Float
 * java.lang.Double
 * java.lang.Boolean
 * java.lang.Character
 * java.lang.Class
 * java.io.File
 * java.net.URL
 * java.net.URI
 * java.lang.Enum [1]
 * java.util.Date
 * java.sql.Date

[1] This enables the conversion of all enum value types

## Options

Options are defined by actions and must provide the following metadata:

  * *Name*; Option names are the way they are addressed prefixed by two '-'s. For instance, an option called `verbose`
  can be set for action `action` by: `action --verbose true`. Boolean values can also be set in a flag-like style, so
  the above can be rewritten as: `action --verbose`.
  * *Alias*; Aliases are single-character (optional) ways of addressing an option. To set an option called `verbose` with
  alias `x` for action `action` we can write `action -x true`. Boolean values can also be set in a flag-like style. so
  the above can be rewritten as: `action -x`.
    * You can set multiple flags at the same time. For instance, should we have options `a`, `x`, and `D`, we can write:
    `action -axD`
  * *Index*; action indexes can be used to address an option without either a name or an alias. For instance if we have
  options `from` and `to` with respective index numbers of `0` and `1` for action `copy`, we can write: `copy a b`
  which will be the equivalent of `copy --from a --to b`.
  * *Requirement status* is a flag to mark an option as requried or optional. Required options must be configured from the
  command line, otherwise the execution will fail.
  * *Option type* is the type of values the option will expect. For instance, in the above `copy` example, the type for
  the `from` option could be `java.io.File`.
  * *Default value*; each option is expected to provide its own default value, so that when the options are left unset,
  they do not behave unexpectedly.

## ExecutorContext

The execution context is the centerpiece of the execution of the application. It holds several other contexts and configures
actions accordingly. You must either instantiate this class, available at `com.agileapes.nemo.exec.ExecutorContext`, or
have it instantiated in some other way.

Having the context executed in some way, you can then bind the startup of your application to the nemo platform:

    public static void main(String[] args) throws Exception {
        final ExecutorContext context = getContext();
        //we must add our actions, value readers, and strategies to the context at this stage.
        context.execute(args);
    }