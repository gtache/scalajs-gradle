package ch.epfl.gtache

import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction

class FastOptJSTask extends JavaExec {
    String description = "Compiles all sjsir files into a single javascript file"
    File srcDir
    File destFile

    @TaskAction
    def fastOptJS() {
        classpath = project.configurations.runtime
        classpath += sourceSets.main.runtimeClasspath
        main = 'Scalajsld'
        inputs.files(srcDir)
        outputs.file(destFile)
        def argsL = new ArrayList<String>()
        argsL.add(srcDir.absolutePath)
        argsL.add(destFile.absolutePath)
        classpath.each { argsL.add(it.absolutePath) }
        args = argsL
    }
}
