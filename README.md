# scalajs-gradle [![Build Status](https://travis-ci.org/gtache/scalajs-gradle.svg?branch=master)](https://travis-ci.org/gtache/scalajs-gradle)

## Requirements
You must `apply plugin: 'scalajs-plugin'` and declare 
```
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.github.gtache:scalajs-plugin:0.2.0'
    }
}
```
to use this plugin.    
*Check the build.gradle of scalajs-plugin-test if needed.*    
Needs .scala files to be in src/main/scala (or [configure your ScalaCompile](https://docs.gradle.org/current/userguide/scala_plugin.html) task accordingly)    
Needs tests to be in src/test/scala.

## Added by the plugin
This plugin adds :   
-`apply plugin: 'java'`   
-`apply plugin: 'scala'`   
as well as dependencies on **scalajs-library 2.11:0.6.9** and **scalajs-compiler 2.11.8:0.6.9**

## Usage
`gradlew FastOptJS`, `gradlew FullOptJS` or `gradlew NoOptJS` to compile everything.

You can run the generated javascript file with `gradlew RunJS`.    
You can run tests with `gradlew TestJS`.

### Options for RunJS
-`-Pclassname` is the fully qualified name of the class to run    
-`-Pmethname` is the method of classname to run.    
-`-PtoExec` (has higher priority than `-Pclassname`) will run the given explicit command    
-`-PfileToExec` (has highest priority) will run the given js file.    
-Adding `-PrunNoOpt` will run the unoptimized file   
-Adding `-PrunFull` will run the fully optimized file (overrides `-PrunNoOpt` if both are used)   
-It will run the fast optimized file by default.  
RunJS will depend on FastOptJS (default), FullOptJS or NoOptJS accordingly.
-Adding `-Pphantom` will run the file in a phantomjs environment (needs phantomjs on path).
-Adding `-Prhino` will run the file in a rhino environment.

Examples : `gradlew RunJS -Pclassname="main.scala.DummyObject"` will compile everything and run DummyObject().main()

`gradlew RunJS -Pclassname="main.scala.DummyObject" -Pmethname="printSomething(\"blabla\")" -PrunFull` will compile the fully optimized version of the files and will run DummyObject().printSomething("blabla")

`gradlew RunJS -PtoExec="main.scala.DummyObject().main() -Pphantom"` will compile everything and run DummyObject().main() in a phantomjs environment.

`gradlew RunJS -PfileToExec="testjs/TestRunWithFile.js"` will run TestRunWithFile.js with the environment loaded with the compiled js file.

### Options for TestJS
-`-PrunFull`, `-PrunNoOpt`, `-Pphantom` and `-Prhino` have the same behavior as with RunJS.


## Changelog    
###0.2.0 (in development)   
-Adds support for RhinoJS and PhantomJS    
-Adds options for the linker    
-Removes useless tasks    
-Adds support for testing    
-Various improvements (cleaning, bugs, etc)    

###0.1.1    
-Adds -PtoExec to input directly what to execute.   
-Linker and Cache are kept alive (should increase speed dramatically). Only works with a gradle daemon.   
-Fixes a problem with the linker not linking when files are changed.

###0.1.0    
-First version
