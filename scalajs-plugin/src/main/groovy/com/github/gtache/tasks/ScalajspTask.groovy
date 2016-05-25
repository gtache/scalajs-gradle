package com.github.gtache.tasks

import com.github.gtache.Scalajsp
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import scala.Option
import scala.collection.JavaConverters

class ScalajspTask extends DefaultTask {

    @TaskAction
    def run() {
        if (project.hasProperty('s') || project.hasProperty('supported')){
            Scalajsp.printSupported()
        } else {
            Scalajsp.Options options = Scalajsp.getDefaultOptions()
            if (project.hasProperty('i') || project.hasProperty('infos')){
                options = options.withInfos(true)
            }
            if (project.hasProperty('filename')){
                String[] filenames = (project.property('filename') as String).split(';')
                options = options.withFileNames(JavaConverters.asScalaSetConverter(filenames.toList().toSet()).asScala()
                        .toSet().toSeq().toIndexedSeq())
            } else if (project.hasProperty('f')){
                String[] filenames = (project.property('f') as String).split(';')
                options = options.withFileNames(JavaConverters.asScalaSetConverter(filenames.toList().toSet()).asScala()
                        .toSet().toSeq().toIndexedSeq())
            }
            if (project.hasProperty('jar')){
                options = options.withJar(Option.apply(project.file(project.property('jar'))))
            } else if (project.hasProperty('j')){
                options = optoins.withJar(Option.apply(project.file(project.property('j'))))
            }
            Scalajsp.execute(options)
        }
    }
}
