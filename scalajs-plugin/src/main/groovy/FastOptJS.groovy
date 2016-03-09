import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction

/**
 * Created by Guillaume on 09.03.2016.
 */
class FastOptJSTask extends JavaExec {
    String description = "Compiles all sjsir files into a single javascript file"
    String srcDir
    String destFile

    @TaskAction
    def fastOptJS() {
        classpath = project.configurations.runtime
        classpath += sourceSets.main.runtimeClasspath
        main = 'Scalajsld'
        def src = project.file(srcDir)
        def dest = project.file(destFile)
        inputs.files(src)
        outputs.file(dest)
        def argsL = new ArrayList<String>()
        argsL.add(src.absolutePath)
        argsL.add(dest.absolutePath)
        classpath.each { argsL.add(it.absolutePath) }
        args = argsL
    }
}
