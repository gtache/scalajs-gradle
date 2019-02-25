package com.github.gtache.tasks

import com.github.gtache.ScalaUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskAction

class SjsirTask extends DefaultTask {

    FileCollection srcFiles

    @TaskAction
    def run() {
        def cp = project.configurations.runtime + srcFiles
        def ret = listSJSIR(cp)
        project.logger.println(ret.collect { it.second }.join("\n"))
    }

    public static List<Tuple2<String, String>> listSJSIR(FileCollection cp) {
        return ScalaUtils.listClasspathC(cp, 'sjsir')
    }
}
