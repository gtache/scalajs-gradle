import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction

class CreateDirsTask extends DefaultTask {
    String description = "Creates necessary directories for compiling scalaJS."
    @OutputFiles
    FileCollection toCreate

    @TaskAction
    def create() {
        toCreate.each {
            if (!it.exists() && !it.mkdir()) {
                logger.error("Couldn\'t create '" + it.name + "' directory")
            }
        }
        doFirst {
            logger.info('Creating directories...')
        }
        doLast {
            logger.info('Directories creation done')
        }
    }
}