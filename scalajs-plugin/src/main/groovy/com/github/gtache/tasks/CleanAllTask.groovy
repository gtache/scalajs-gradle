package com.github.gtache.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files

/**
 * Task used to delete everything in a given FileCollection
 */
class CleanAllTask extends DefaultTask {
    final String description = "Deletes all files given in toDelete."

    FileCollection toDelete

    /**
     * Deletes a file, and if it is a folder, deletes it recursively.
     * @param file The file to be deleted
     * @return Unit
     */
    def deleteFile(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                file.listFiles().each { deleteFile(it) }
            }
            logger.info("Deleting : " + file.name)
            if (!file.canWrite()) {
                logger.info("Couldn't delete file " + file.name + " : File is locked ?")
            } else {
                Files.deleteIfExists(file.toPath())
            }
        } else {
            logger.info("Couldn't delete file " + file.name + " : File doesn't exist.")
        }
    }

    /**
     * Main method of the task, simply calls deleteFile on toDelete
     */
    @TaskAction
    def run() {
        if (toDelete != null) {
            toDelete.each { deleteFile(it) }
        }
    }
}
