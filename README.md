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
and dependencies on **scalajs-library 2.11:0.6.9**, **scalajs-compiler 2.11.8:0.6.9**, as well as **org.eclipse.jetty:jetty-server:8.1.16.v20140903** and **org.eclipse.jetty:jetty-websocket:8.1.16.v20140903** for PhantomJS

## Usage
`gradlew FastOptJS`, `gradlew FullOptJS` or `gradlew NoOptJS` to compile everything.

You can run the generated javascript file with `gradlew RunJS`.    
You can run tests with `gradlew TestJS`. Be aware that this is still an early feature.

## Test frameworks supported     
-ScalaTest    
-JUnit ***(you have to add the junit-test-plugin to the compileTestScala task)***    
-Minitest    
-utest (not fully supported, the summary will print the tests as Unknown and the retest feature will ignore them)   
-ScalaProps    
You can mix them (Have a JUnit suite with a utest suite and a ScalaTest suite, etc)    
You must obviously add the dependencies and TestFrameworks for those to work.    

To add the JUnit plugin or the dependencies, please refer to the *build.gradle* in *scalajs-test-plugin* or to the snippet below.

### Options for the linker (Fast/Full/NoOptJS)
-`-Po="pathtofile" | -Poutput="pathtofile"` to change where the js file will be generated **There will only be one file for every different build (fast, full, test or not)**    
-`-Pp | -Pprettyprint` for prettyPrint    
-`-Ps | -Psourcemap` for sourceMap    
-`-PcompliantAsInstanceOfs`    
-`-Pm=NameOfMode | -PoutputMode=NameOfMode` to change the output mode (ECMAScript51Global, ECMAScript51Isolated, ECMAScript6)    
-`-Pc | -PcheckIR`    
-`-Pr="PathToSourceMap" | -PrelativizeSourceMap="PathToSourceMap"` to add a sourceMap     
-`-PlinkLogLevel=Debug|Info (default)|Warn|Error` to change the level of logging    
-`-Pd | -Pdebug` for Debug level    
-`-Pq | -Pquiet` for Warn level    
-`-Pqq | -Preally-quiet` for Error level    

### Options for RunJS
-`-Pclassname` is the fully qualified name of the class to run    
-`-Pmethname` is the method of classname to run.    
-`-PtoExec` (has higher priority than `-Pclassname`) will run the given explicit command    
-`-PfileToExec` (has highest priority) will run the given js file.    
-Adding `-PrunNoOpt` will run the unoptimized file   
-Adding `-PrunFull` will run the fully optimized file      
-It will run the fast optimized file by default.  
RunJS will depend on FastOptJS (default), FullOptJS or NoOptJS accordingly.    
-Adding `-Pphantom` will run the file in a phantomjs environment (needs phantomjs on path).    
-Adding `-Prhino` will run the file in a rhino environment.    
-You can change the level of logging with `-PrunLogLevel=Warn` for example.   

Examples : `gradlew RunJS -Pclassname="main.scala.DummyObject"` will compile everything and run DummyObject().main()

`gradlew RunJS -Pclassname="main.scala.DummyObject" -Pmethname="printSomething(\"blabla\")" -PrunFull` will compile the fully optimized version of the files and will run DummyObject().printSomething("blabla")

`gradlew RunJS -PtoExec="main.scala.DummyObject().main() -Pphantom"` will compile everything and run DummyObject().main() in a phantomjs environment.

`gradlew RunJS -PfileToExec="testjs/TestRunWithFile.js"` will run TestRunWithFile.js with the environment loaded with the compiled js file.

### Options for TestJS
-`-PrunFull`, `-PrunNoOpt`, `-Pphantom` and `-Prhino` have the same behavior as with RunJS.    
-`-Ptest-only=class1;class2;*l*s3` and -`-Ptest-quick=...` should have the same behavior as their sbt counterparts. **You can only select classes / suites at the moment, you can't select tests.**  
-`-Pretest` should retest all failed tests (does not work with Utest).    
You can change the level of logging with `-PtestLogLevel=Error` for example.   
*Note that retest / test-quick need a Gradle daemon to work*.

### Making options 'permanent'
Don't forget that you can set the options directly in build.gradle. Simply put the property in the 'ext' closure.   
Example : Instead of writing -PtestLogLevel=Debug -Po="generated.js" -Pd -PfileToExec="toExec/exec.js" everytime, write  

```
ext {    
    testLogLevel="Debug" //Use a string
    o="generated.js"
    d=true //or false, or whatever, it just checks that the property exists
    fileToExec="toExec/exec.js"
    testFrameworks=["utest.runner.Framework","minitest.runner.Framework"] //If you need to add TestFrameworks
}
```

### Possible problems
-*Permission denied*    
=> Solution : apply chmod +x to gradlew    
-*GC overhead limit exceeded* when running CompileJS    
=> Solution : edit gradle.properties in %USER%/.gradle/ with `org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=4096m -XX:+HeapDumpOnOutOfMemoryError` (or tweak the numbers) (source : http://stackoverflow.com/questions/27164452/how-to-solve-java-lang-outofmemoryerror-gc-overhead-limit-exceeded-error-in-and)    
(Don't forget to delete the hprof file in the project folder)    
-Something related to the linker state, after a failure while linking    
=> Solution : `gradlew --stop` to stop the daemon containing the linker. Then rerun the desired command as usual.

## Changelog    
###0.2.0  
-Adds support for RhinoJS and PhantomJS    
-Adds options for the linker    
-Removes useless tasks    
-Adds support for basic testing    
-Various improvements (cleaning, bugs, etc)    

###0.1.1    
-Adds -PtoExec to input directly what to execute.   
-Linker and Cache are kept alive (should increase speed dramatically). Only works with a gradle daemon.   
-Fixes a problem with the linker not linking when files are changed.

###0.1.0    
-First version
