package com.github.gtache

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task used to add an executable line at the end of a file
 */
class AddMethExecTask extends DefaultTask {
    final String description = "Adds the main exec at the end of the js file, given the object name, assuming function to run is \"main\" (default)\n" +
            "Depends on copyJS\n" +
            "Usage : \"gradlew addMainExec -Pclassname=\'nameOfClass\' -Pmethname=\'nameOfMethod\'"
    @InputFile
    @OutputFile
    File srcFile

    /**
     * Main method of the task, adds the executable line at the end of the file.
     */
    @TaskAction
    def addMethExec() {
        if (!project.properties.containsKey('classname')) {
            logger.info('Skipping addMainExec : no classname given')
        } else {
            final def classname = project.properties.get('classname');
            if (srcFile.exists() && srcFile.canWrite()) {
                String toAdd
                if (!project.properties.containsKey('methname')) {
                    toAdd = classname + '().main()'
                } else {
                    toAdd = classname + '().' + project.properties.get('methname')
                }
                logger.info('Adding ' + toAdd + ' at the end of the file.')
                srcFile.append('\n' + toAdd)
                logger.info('Done')
            } else {
                logger.error('Couldn\'t find or write ' + srcFile.path)
            }
        }
    }
}
