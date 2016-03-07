# scalajs-gradle [![Build Status](https://travis-ci.org/gtache/scalajs-gradle.svg?branch=master)](https://travis-ci.org/gtache/scalajs-gradle)
It is advised to use the **scalajs-test** project.

Needs .scala files to be in src/main/scala (and scalajs files in src/main/scalajs)

Usage : *gradlew compileJS* to compile everything.

*gradlew compileSJSIR* to create the related sjsir and class files.

Simply *gradlew* to run the default compileJS task.

There is also *gradlew cleanAll* which will delete the build, sjsir and js directories.

You can run the generated javascript file with *gradlew runJS -Pclassname='nameOfClass' -Pmethname='nameOfMeth'*, where Pclassname is the full name of the class (normally main.scalajs.NAME) and where Pmethname is optional (default is main). It requires Node.js to be installed.

Examples : *gradlew runJS -Pclassname=main.scalajs.Test* will compile everything and run Test().main() (With the given files, it should print the square of 10)

*gradlew runJS -Pclassname=main.scalajs.Test -Pmethname=printSomething(\"blabla\")* will compile all the files and will run Test().printSomething("blabla"), which should print "blabla" with the given files.
