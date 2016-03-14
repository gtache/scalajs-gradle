# scalajs-gradle [![Build Status](https://travis-ci.org/gtache/scalajs-gradle.svg?branch=master)](https://travis-ci.org/gtache/scalajs-gradle)
Use the **scalajs-plugin-test** project.

Needs .scala files to be in src/main/scala
You must run *gradlew install* in scalajs-plugin (it installs the plugin on local maven repository)

Usage : *gradlew FastOptJS* or *gradlew FullOptJS* to compile everything.

Simply *gradlew* to run the default FastOptJS task.

There is also *gradlew cleanAll* which will delete the build and js directories.

You can run the generated javascript file with *gradlew runJS -Pclassname='nameOfClass' -Pmethname='nameOfMeth'*, where Pclassname is the full name of the class (normally main.scala.NAME) and where Pmethname is optional (default is main). It requires Node.js to be installed.

Examples : *gradlew runJS -Pclassname=main.scala.Test* will compile everything and run Test().main() (With the given files, it should print the square of 10)

*gradlew runJS -Pclassname=main.scala.Test -Pmethname=printSomething(\"blabla\")* will compile all the files and will run Test().printSomething("blabla"), which should print "blabla" with the given files.
