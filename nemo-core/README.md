## Under the Hood

In this section we will go over the inner workings of **nemo** and see how it performs its actions. This will help you
understand how you can interact with the framework in the best possible manner.

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