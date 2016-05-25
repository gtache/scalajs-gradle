package com.github.gtache.tasks

import com.github.gtache.Scalajsp
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import scala.Option
import scala.collection.JavaConverters

/**
 * Task used to call scalajsp
 */
class ScalajspTask extends DefaultTask {
    final String description = "Translates and prints sjsir file to a more readable format"

    public static final String MIN_SUPPORTED = 's'
    public static final String SUPPORTED = 'supported'
    public static final String MIN_INFOS = 'i'
    public static final String INFOS = 'infos'
    public static final String MIN_FILENAME = 'f'
    public static final String FILENAME = 'filename'
    public static final String MIN_JAR = 'j'
    public static final String JAR = 'jarfile' //can't use jar as it is the name of a task apparently...

    /**
     * Parametrize the options and calls scalajsp
     */
    @TaskAction
    def run() {
        if (project.hasProperty(MIN_SUPPORTED) || project.hasProperty(SUPPORTED)) {
            Scalajsp.printSupported()
        } else {
            Scalajsp.Options options = Scalajsp.defaultOptions()
            if (project.hasProperty(MIN_INFOS) || project.hasProperty(INFOS)) {
                options = options.withInfos(true)
            }
            if (project.hasProperty(MIN_FILENAME)) {
                String[] filenames = (project.property(MIN_FILENAME) as String).split(';')
                options = options.withFileNames(JavaConverters.asScalaSetConverter(filenames.toList().toSet()).asScala()
                        .toSet().toSeq().toIndexedSeq())
            } else if (project.hasProperty(FILENAME)) {
                String[] filenames = (project.property(FILENAME) as String).split(';')
                options = options.withFileNames(JavaConverters.asScalaSetConverter(filenames.toList().toSet()).asScala()
                        .toSet().toSeq().toIndexedSeq())
            }
            if (project.hasProperty(MIN_JAR)) {
                options = options.withJar(Option.apply(project.file(project.property(MIN_JAR))))
            } else if (project.hasProperty(JAR)) {
                options = options.withJar(Option.apply(project.file(project.property(JAR))))
            }
            Scalajsp.execute(options)
        }
    }
}
