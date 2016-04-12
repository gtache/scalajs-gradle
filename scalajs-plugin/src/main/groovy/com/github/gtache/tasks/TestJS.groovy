package com.github.gtache.tasks

import com.github.gtache.Utils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.scalajs.core.tools.logging.Level
import org.scalajs.jsenv.ConsoleJSConsole$
import org.scalajs.testadapter.ScalaJSFramework

class TestJSTask extends DefaultTask {
    final String description = "Runs tests"

    @TaskAction
    def run() {
        final ScalaJSFramework framework = new ScalaJSFramework(
                "ScalaJS Testing framework",
                Utils.resolveEnv(project),
                Utils.resolveLogLevel(project, 'testLogLevel', Level.Debug$.MODULE$),
                ConsoleJSConsole$.MODULE$)
    }


}
