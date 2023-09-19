# Gradle plugin for ScalaJS #

## Summary ##

This is a Gradle plugin for working with Scala.js.
It supports linking ScalaJS code.

This plugin also supports testing plain Scala code (no ScalaJS) using sbt-compatible testing frameworks.

Supports ScalaJS 1; default version: 1.9.0.

Plugin requires Gradle 7.5.0.

Plugin is written in Scala 2.


## Applying to a Gradle project ##

Plugin is [published](https://plugins.gradle.org/plugin/io.github.machaval.scalajs)
on the Gradle Plugin Portal. To apply it to a Gradle project:

```groovy
plugins {
  id 'io.github.machaval.scalajs' version '2.0.0'
}
```

Plugin will automatically apply the Scala plugin to the project, so there is no need to manually list
`id 'scala'` in the `plugins` block - but there is no harm in it either.
Either way, it is the responsibility of the project using the plugin to add a standard Scala library
dependency that the Scala plugin requires.

Plugin forces resolution of the `implementation` and `testImplementation` configurations
and some others and must be thus applied *after* any plugins that add dependencies to those configurations.
One such plugin is the Gradle Plugin Portal Publishing Plugin, which applies Gradle Plugin Plugin,
which adds dependencies to configurations.

## ScalaJS ##

### ScalaJS compiler ###
To support ScalaJS, Scala compiler needs to be configured.

ScalaJS compiler plugin dependency needs to be declared:
```groovy
dependencies {
  scalaCompilerPlugins "org.scala-js:scalajs-compiler_$scalaVersion:1.11.0"
}
```

Plugin does this automatically unless a dependency on `scala-compiler` is declared explicitly.

To enable Scala compiler plugins, their classpaths need to be given to the compiler
via a `-Xplugin:` option. Examples of the Gradle build script code that do that abound:

```groovy
tasks.withType(ScalaCompile) {
  scalaCompileOptions.additionalParameters = [
    '-Xplugin:' + configurations.scalaCompilerPlugin.asPath
  ]
}
```

*Note:* Such code is not needed, since Gradle Scala plugin already does this.

### Dependencies ###

Plugin uses some dependencies internally:
- ScalaJS linker;

ScalaJS compiler plugin is needed.

Plugin also needs some dependencies on the runtime classpath:
- ScalaJS library;

Plugin adds missing dependencies automatically.

Plugin is compiled against specific versions of ScalaJS
but uses the versions configured in the `scalajs` configuration that it creates.

If you declare a `scalajs-library` dependency explicitly, plugin chooses the same
version for the ScalaJS dependencies it adds
(`scalajs-linker`, `scalajs-compiler`).

Example with explicit dependencies:
```groovy
final String scalaVersion       = '2.12.15'
final String scala2versionMinor = '2.12'
final String scalaJsVersion     = '1.9.0'

dependencies {
  implementation "org.scala-lang:scala-library:$scalaVersion"
  implementation "org.scala-js:scalajs-library_$scala2versionMinor:$scalaJsVersion"
  
  scalajs "org.scala-js:scalajs-linker_$scala2versionMinor:$scalaJsVersion"
  
  scalaCompilerPlugins "org.scala-js:scalajs-compiler_$scalaVersion:$scalaJsVersion"
}
```

And - with only the required dependencies:
```groovy
final String scalaVersion       = '2.12.15'

dependencies {
  implementation "org.scala-lang:scala-library:$scalaVersion"
}
```

### Linking ###
For linking of the main code, plugin adds `link` task of type `org.machaval.tools.scalajs.Link.Main`.
All tasks of this type automatically depend on the `classes` task.

Each of the tasks exposes a property `JSDirectory` that points to a directory
with the resulting JavaScript, so that it can be copied where needed.
For example:

```groovy
link.doLast {
  project.sync {
    from link.JSDirectory
    into jsDirectory
  }
}
```

Link tasks have a number of properties that can be used to configure linking.
Configurable properties with their defaults are:

```groovy
link {
  optimization     = 'Fast'          // one of: 'Fast', 'Full'
  moduleKind       = 'NoModule'      // one of: 'NoModule', 'ESModule', 'CommonJSModule'
  moduleSplitStyle = 'FewestModules' // one of: 'FewestModules', 'SmallestModules'
  prettyPrint      = false
  optimizer        = true
  esVersion        = 'ECMAScript 2018 (edition 9)'
}
```

Setting `optimization` to `Full`:
- uses `Semantics.optimized`;
- enables `checkIR`;
- enables Closure Compiler (unless `moduleKind` is set to `ESModule`).

For `Link.Main` tasks, a list of module initializers may also be configured:

```groovy
moduleInitializers {
  main { 
    className = '<fully qualified class name>'
    mainMethodName = 'main'
    mainMethodHasArgs = false
  }
  //...
}
```

Name of the module initializer ('main' in the example above) becomes the module id.
