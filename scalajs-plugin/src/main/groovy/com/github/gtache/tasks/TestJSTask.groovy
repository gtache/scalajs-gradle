package com.github.gtache.tasks

import com.github.gtache.Utils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.scalajs.core.tools.logging.Level
import org.scalajs.jsenv.ConsoleJSConsole$
import org.scalajs.testadapter.ScalaJSFramework
import org.scalajs.testadapter.ScalaJSRunner
import sbt.testing.Logger
import sbt.testing.Task
import sbt.testing.TaskDef

class TestJSTask extends DefaultTask {
    final String description = "Runs tests"

    @TaskAction
    def run() {
        final ScalaJSFramework framework = new ScalaJSFramework(
                "ScalaJS Testing framework",
                Utils.resolveEnv(project),
                Utils.resolveLogLevel(project, 'testLogLevel', Level.Debug$.MODULE$),
                ConsoleJSConsole$.MODULE$)
        final ScalaJSRunner runner = framework.runner(new String[0], new String[0], null)
        final Task[] tasks = runner.tasks(new TaskDef[0])
        for (Task t : tasks) {
            t.execute(null, new Logger[0])
        }
        framework.runDone()
    }


}
