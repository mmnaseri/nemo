Nemo
====

Nemo is a framework for creating Java-based command-line applications using an easy-to-grasp
conceptualization of the command-line environment.


Introduction
------------

Nemo will help you build your command-line application in Java, without doing any configuration
or time-consuming string processing or tokenizing of input parameters.

Everything is available to you out of the box. All you need to do is create a Spring XML
application context file under `/nemo/exec*.xml`, and extend `com.agileapes.nemo.action.Action`
to add your very own targets to the command line tool.

An easy setup for a sample project has been provided under the `demo` module, which you
can -- and, indeed, are encouraged to -- read through to see how it is done.

Concept
-------

The concept is fairly easy to grasp. We all -- most probably -- have worked with one or other
command line shell under *nix operating systems, and we all love -- don't we? ;-) -- the
various options they make available to you.

Simple command line applications, say `cp`, do just the one thing. Thus, you just pass in options
as their input, and they do what they are supposed to do for you:

    cp -r ~/test /tmp/

This application is -- in the terminology of nemo -- a single-targeted application. It does
*only one thing*.

However, more complex applications, say `git`, can do many things, and they are most usually
configured by first saying *what* it is that you want to do, and then configuring the target
you have specified:

    git clone --bare -l /home/proj/.git /pub/scm/proj.git

Here, you first tell `git` that you want to run and configure the `clone` target.

The `clone` target in turn first receives the flag `--bare`, and then taks in the option
`-l` (an alias for `--local`) with the value `/home/proj/.git` and later on takes its main,
indexed option `/pub/scm/proj.git`.

Example
-------

The above example can be achieved quite simply using nemo by first writing the action class:

    public class CloneAction extends Action {
        //lines omitted

        @Option
        private boolean bare;

        @Option(alias = 'l')
        private File local;

        @Option(required = true, index = 0)
        private URL path;
        //lines omitted
    }

and then including this action with the target name `clone`:

    <bean id="clone" class="....CloneAction" />

Values
------

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