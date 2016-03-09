import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by Guillaume on 09.03.2016.
 */
class AddMethExecTask extends DefaultTask {
    String description = "Adds the main exec at the end of the js file, given the object name, assuming function to run is \"main\" (default)\n" +
            "Depends on copyJS\n" +
            "Usage : \"gradlew addMainExec -Pclassname=\'nameOfClass\' -Pmethname=\'nameOfMethod\'"
    String srcFile
    String classname
    String methname = 'main()'

    @TaskAction
    def addMethExec() {
        doFirst {
            if (!project.properties.containsKey('classname')) {
                logger.info('Skipping addMainExec : no classname given')
            } else {
                def js = project.file(srcFile)
                if (js.exists() && js.canWrite()) {
                    def toAdd
                    if (!project.properties.containsKey('methname')) {
                        toAdd = classname + '().main()'
                    } else {
                        toAdd = classname + '().' + methname
                    }
                    logger.info('Adding ' + toAdd + ' at the end of the file.')
                    js.append('\n' + toAdd)
                    logger.info('Done')
                } else {
                    logger.error('Couldn\'t find or write ' + js.path)
                }
            }
        }
    }
}
