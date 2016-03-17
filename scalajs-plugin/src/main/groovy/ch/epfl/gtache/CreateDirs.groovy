package ch.epfl.gtache

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.TaskAction

class CreateDirsTask extends DefaultTask {
    final String description = "Creates necessary directories for compiling scalaJS."
    @OutputDirectories
    FileCollection toCreate

    @TaskAction
    def create() {
        logger.info('Creating directories...')
        toCreate.each {
            if (!it.exists() && !it.mkdirs()) {
                logger.error("Couldn\'t create '" + it.name + "' directory")
            }
        }
        logger.info('Directories creation done')
    }
}