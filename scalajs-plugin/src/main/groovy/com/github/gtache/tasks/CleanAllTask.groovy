package com.github.gtache.tasks

import com.github.gtache.Utils
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskAction

/**
 * Task used to delete everything in a given FileCollection
 */
public class CleanAllTask extends DefaultTask {
    final String description = "Deletes all files given in toDelete."

    FileCollection toDelete

    /**
     * Main method of the task, simply calls deleteRecursive on toDelete
     */
    @TaskAction
    def run() {
        if (toDelete != null) {
            toDelete.each { Utils.deleteRecursive(it) }
        }
    }
}
