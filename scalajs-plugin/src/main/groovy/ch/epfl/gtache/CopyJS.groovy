package ch.epfl.gtache

import org.gradle.api.tasks.Copy

class CopyJSTask extends Copy {
    final String description = "Copy the generated js file while renaming it with \'_exec.js\' at the end." +
            " Used by addMethExec for runJS."

    public CopyJSTask() {
        rename {
            String filename -> filename.replace('.js', '_exec.js')
        }
    }
}
