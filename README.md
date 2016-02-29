# scalajs-gradle
Only the **testcmd** "branch" is "working" right now.

Needs scala and scalac commands on PATH, and .scala files to be in "src/main/scala"

Usage : "gradlew compileJS -Pfile='nameOfFile'" or "gradlew compileJS" to compile all of them

"gradlew compileSJSIR -Pfile='nameOfFile'" or "gradlew compileSJSIR" to create the related sjsir and class files.

Simply "gradlew" to run the default "compileJS" task.