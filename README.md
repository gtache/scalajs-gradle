# scalajs-gradle
Only the **testcmd** project is working right now.

Needs scala and scalac commands on PATH, and .scala files to be in "src/main/scala"

Usage : "gradlew compileJS -Pfile='nameOfFile'" or "gradlew compileJS" to compile all of them

"gradlew compileSJSIR -Pfile='nameOfFile'" or "gradlew compileSJSIR" to create the related sjsir and class files.

Simply "gradlew" to run the default "compileJS" task.

You can run the generated javascript file with "gradlew runJS -Pclassname='nameOfClass' -Pmethname='nameOfMeth'", where Pmethname is optional (default is 'main'). It requires Node.js to be installed.

Examples : -"gradlew runJS -Pfile=test -Pclassname=Test" will only compile test.scala and will run Test().main() (With the given files, it should print the square of 10)

-"gradlew runJS -Pclassname=Test2 -Pmethname=printSomething(\"blabla\")" will compile all the scala files in src/main/scala and will run Test2().printSomething("blabla"), which should print "blabla" with the given files.
