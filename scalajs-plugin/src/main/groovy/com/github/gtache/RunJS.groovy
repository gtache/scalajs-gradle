package com.github.gtache

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.tasks.Exec

public class RunJSTask extends Exec {
    final String description = "Runs the generated js file.\n" +
            "Depends on addMainExec.\n" + "Needs Node.js on PATH."
    String toExec

    public RunJSTask() {
        executable = Os.isFamily(Os.FAMILY_WINDOWS) ? 'cmd' : 'node'
    }

    def inferArgs() {
        args = executable == 'cmd' ? ['/C', 'node', toExec] : [toExec]
        logger.info('Configuring RunJS with ' + executable + ' and args ' + args)
    }
}
