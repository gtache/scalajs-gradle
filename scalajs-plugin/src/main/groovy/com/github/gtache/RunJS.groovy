package com.github.gtache

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.tasks.Exec

/**
 * Task used to run a js file (requires Node.js)
 */
public class RunJSTask extends Exec {
    final String description = "Runs the generated js file.\n" +
            "Depends on addMainExec.\n" + "Needs Node.js on PATH."
    String toExec

    /**
     * Constructor of the task
     */
    public RunJSTask() {
        executable = Os.isFamily(Os.FAMILY_WINDOWS) ? 'cmd' : 'node'
    }

    /**
     * Configures the args depending on being on Unix or Windows
     */
    def inferArgs() {
        args = executable == 'cmd' ? ['/C', 'node', toExec] : [toExec]
        logger.info('Configuring RunJS with ' + executable + ' and args ' + args)
    }
}
