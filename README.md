# scalajs-gradle [![Build Status](https://travis-ci.org/gtache/scalajs-gradle.svg?branch=master)](https://travis-ci.org/gtache/scalajs-gradle)

## Requirements
You must `apply plugin: 'scalajs-plugin'` and declare 
```
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.github.gtache:scalajs-plugin:0.1.0'
    }
}
```
to use this plugin.
*Check the build.gradle of scalajs-plugin-test if needed*
Needs .scala files to be in src/main/scala (or [configure your ScalaCompile](https://docs.gradle.org/current/userguide/scala_plugin.html) task accordingly)
(Optional) [Node.js](https://nodejs.org/) to run the generated JS file.

## Usage
`gradlew FastOptJS`, `gradlew FullOptJS` or `gradlew NoOptJS` to compile everything.

Simply `gradlew` to run the default FastOptJS task.

There is also `gradlew CleanAll` which will delete the build and js directories.

You can run the generated javascript file with `gradlew runJS -Pclassname='nameOfClass' -Pmethname='nameOfMeth'`, where Pclassname is the full name of the class (main.scala.*name* in the scalajs-plugin-test project) and where Pmethname is optional (default is main). It requires Node.js to be on PATH.

### Options for RunJS
-Adding `-PrunNoOpt` will copy and run the unoptimized file   
-Adding `-PrunFull` will copy and run the fully optimized file (overrides `-PrunNoOpt` if both are used)   
-It will copy and run the fast optimized file by default.  
RunJS will depend on FastOptJS (default), FullOptJS or NoOptJS accordingly.

Examples : `gradlew runJS -Pclassname=main.scala.Test` will compile everything and run Test().main() (With the scalajs-plugin-test, it should print the square of 10)

`gradlew runJS -Pclassname=main.scala.Test -Pmethname=printSomething(\"blabla\") -PrunFull` will compile the fully optimized version of the files and will run Test().printSomething("blabla"), which should print "blabla" with the scalajs-plugin-test project.
