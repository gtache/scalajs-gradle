package ch.epfl.gtache

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

public class RunJSTask extends Exec {
    String description = "Runs the generated js file.\n" +
            "Depends on addMainExec.\n" + "Needs Node.js on PATH."
    File toExec

    public RunJSTask(){
        executable = Os.isFamily(Os.FAMILY_WINDOWS) ? 'cmd' : 'node'
    }

    def inferArgs() {
        args = executable == 'cmd' ? ['/C', 'node', toExec] : [toExec]
    }
}
