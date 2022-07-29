# Scala JS plugin for gradle

This is fork from the [gtache plugin](https://github.com/gtache/scalajs-gradle) as it is no longer supported I decided to fork it upgrade to work with gradle 7.5. 

## Requirements
You must `apply plugin: 'io.github.machaval.scalajs'`.    

## Required dependencies

The user needs to define what version of the compiler and scala library wants to use. The scala compiler needs to be add with `scalaCompilePlugin` scope.

* For example:

```groovy
implementation 'org.scala-js:scalajs-library_2.12:0.6.33'
scalaCompilePlugin 'org.scala-js:scalajs-compiler_2.12.10:0.6.33'
```

## Added by the plugin
This plugin adds :   
-`apply plugin: 'java'`   
-`apply plugin: 'scala'`


## Usage

`gradlew FastOptJS`, `gradlew FullOptJS` or `gradlew NoOptJS` to compile everything.

You can run tests with `gradlew TestJS`. Be aware that this is still an early feature.    

## Test frameworks supported
-ScalaTest    
-JUnit ***(you have to add the junit-test-plugin to the compileTestScala task)***    
-Minitest    
-utest (not fully supported, the summary will print the tests as Unknown and the retest feature will ignore them)   
-ScalaProps    
You can mix them (Have a JUnit suite with a utest suite and a ScalaTest suite, etc)    
You must obviously add the dependencies and TestFrameworks for those to work.

To add the JUnit plugin or the dependencies, please refer to the *build.gradle* in *scalajs-test-plugin*.

### Options for the linker (Fast/Full/NoOptJS)
-`-Po="pathtofile" | -Poutput="pathtofile"` to change where the js file will be generated **This means that there will only be one file for every different build (fast, full, test or not)**    
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
-`-Pbatch` to turn on batch mode    
-`-PnoParallel` to set parallel to false

### Options for TestJS
-`-PrunFull`, `-PrunNoOpt`, `-Pphantom`, `-PjsDom` and `-Prhino` have the same behavior as with RunJS.    
-`-Ptest-only=class1;class2;*l*s3` and -`-Ptest-quick=...` should have the same behavior as their sbt counterparts. **You can only select classes / suites at the moment, you can't select tests.**  
-`-Pretest` should retest all failed tests (does not work with Utest).    
You can change the level of logging with `-PtestLogLevel=Error` for example.   
*Note that retest / test-quick need a Gradle daemon to work*.

```
ext {    
    testLogLevel="Debug" //Use a string
    o="generated.js"
    d=true //or false, or whatever, it just checks that the property exists
    fileToExec="toExec/exec.js"
    testFrameworks=["utest.runner.Framework","minitest.runner.Framework"] //If you need to add TestFrameworks
}
```