package com.github.gtache.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.TaskAction

/**
 * Task used to create directories
 */
class CreateDirsTask extends DefaultTask {
    final String description = "Creates necessary directories for compiling scalaJS."
    @OutputDirectories
    FileCollection toCreate

    /**
     * Main method of the task, create a directory for each file in toCreate
     */
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