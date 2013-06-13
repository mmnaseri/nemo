# Project Nemo

Nemo is a framework for creating Java-based command-line applications using an easy-to-grasp
conceptualization of the command-line environment.

## Introduction

Nemo will help you build your command-line application in Java, without doing any configuration
or time-consuming string processing or tokenizing of input parameters.

Everything is available to you out of the box. All you need to do is create a Spring XML
application context file under `/nemo/exec*.xml`, and extend `com.agileapes.nemo.action.Action`
to add your very own targets to the command line tool.

An easy setup for a sample project has been provided under the `demo` module, which you
can -- and, indeed, are encouraged to -- read through to see how it is done.

## Basics

Here, we will go over the basics of this framework, and will cover just enough material to have a quick get-off.

### Concept

The concept is fairly easy to grasp. We all -- most probably -- have worked with one or other
command line shell under *nix operating systems, and we all love -- don't we? ;-) -- the
various options they make available to you.

Simple command line applications, like `cp`, do just the one thing. Thus, you just pass in options
as their input, and they do what they are supposed to do for you:

    cp -r ~/test /tmp/

This application is -- in the terminology of nemo -- a single-targeted application. It does
*only one thing*, i.e. it has only one target action and that is its default action.

However, more complex applications, say `git`, can do many things, and they are most usually
configured by first saying *what* it is that you want to do, and then configuring the target
you have specified:

    git clone --bare -l /home/proj/.git /pub/scm/proj.git

Here, you first tell `git` that you want to run and configure the `clone` target.

The `clone` target in turn first receives the flag `--bare`, and then taks in the option
`-l` (an alias for `--local`) with the value `/home/proj/.git` and later on takes its main,
indexed option `/pub/scm/proj.git`.

### Goal

The goal of this framework is to make it as easy as possible to create just such applications, which can receive
configuration as input through command-line options, without having to go through all the hassle of writing a
whole argument parser/cofigurer for yourself.

This framework will also make the naturally functional method of invoking actions through command-line addressing
a more object-oriented-friendly process.

#### Example

The above example about `git clone` can be achieved quite simply using nemo by first writing the action class:

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

**Note** that this is only one way of defining an action; but as this is the basics section, you need not worry about
the specifics of that just yet. This is one way, but it is a way that works most easily.

### Hello World

To get a better understanding of how we could achieve a simple execution via command line, we will write a hello action,
that accepts a target called 'hello' with an optional argument named `name`.

Before writing any code you will have to make sure that you have added the proper dependency for `nemo-core` module to your
project.

First, we write the action class:

    public class HelloAction extends Action {

        @Option
        private String name = "somebody";

        public void execute() throws Exception {
            getOutput().println("Hello, " + name);
        }

    }

Now, having defined the action, we will register it with the executor and run it through the main method:

    public class HelloNemo {

        public static void main(String... args) {
            final ExecutorContext context = new ExecutorContext();
            context.addAction("hello", new HelloAction());
            context.execute(args);
        }

    }

After building a JAR file out of this application (which here we assume is named `hello.jar`) we can invoke it thus:

    java -jar hello.jar hello

Which will output:

    Hello, somebody

As we would have expected, or invoke it by passing the option:

    java -jar hello.jar hello --name "Good Guy"

Which will output:

    Hello, Good Guy

### Hello World with Spring

To extend the above example to include Spring integration, we will have to use the `nemo-spring` module.

After that, assuming that you have created a Spring application context configuration XML document at
`/execution/config.xml`, you will have to include these lines inside your application context:

    <import resource="classpath*:/nemo/nemo.xml"/>
    <bean name="hello" class="HelloAction"/>

and in your `main` method, you can write:

    public static void main(String[] args) throws Exception {
        new ClassPathXmlApplicationContext("/execution/config.xml").getBean(ExecutorContext.class).execute(args);
    }


Building a JAR of this artifact will result in the same outputs as above.

For now, we can assume that this information will suffice for most usages of this framework.
