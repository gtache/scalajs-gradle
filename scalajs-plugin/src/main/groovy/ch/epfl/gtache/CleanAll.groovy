package ch.epfl.gtache

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files

class CleanAllTask extends DefaultTask {
    
    FileCollection toDelete

    String description = "Deletes all files given in toDelete."

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
                Files.delete(file.toPath())
            }
        } else {
            logger.info("Couldn't delete file " + file.name + " : File doesn't exist.")
        }
    }

    @TaskAction
    def cleanAll() {
        if (toDelete != null) {
            toDelete.each { deleteFile(it) }
        }
    }
}
