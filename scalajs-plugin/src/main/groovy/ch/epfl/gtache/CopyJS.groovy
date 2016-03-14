package ch.epfl.gtache

import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskAction

class CopyJSTask extends Copy {
    String description = "Copy the generated js file while renaming it with \'_exec.js\' at the end. Used by addMethExec for runJS."

    CopyJSTask(){
        rename {
            String filename -> filename.replace('.js', '_exec.js')
        }
    }
    @TaskAction
    def copyJS() {
        project.logger.info('Copying and renaming js file')
    }
}
