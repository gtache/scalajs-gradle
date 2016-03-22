package com.github.gtache

import org.gradle.api.tasks.Copy

/**
 * Task used to copy and rename a js file
 */
class CopyJSTask extends Copy {
    final String description = "Copy the generated js file while renaming it with \'_exec.js\' at the end." +
            " Used by addMethExec for runJS."

    /**
     * Constructor of the task, used to configure the renaming
     */
    public CopyJSTask() {
        rename {
            String filename -> filename.replace('.js', '_exec.js')
        }
    }
}
