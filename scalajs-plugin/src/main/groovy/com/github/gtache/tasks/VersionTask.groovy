package com.github.gtache.tasks

import com.github.gtache.BuildConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class VersionTask extends DefaultTask {
    final String description = "Prints info regarding the tools versions"

    @TaskAction
    def run() {
        final String scalajsVersion = BuildConfig.SCALAJS_VERSION
        final String scalaVersion = BuildConfig.SCALA_FULL_VERSION
        final String pluginVersion = BuildConfig.PLUGIN_VERSION
        project.logger.println("Scala version : " + scalaVersion)
        project.logger.println("ScalaJS version : " + scalajsVersion)
        project.logger.println("Gradle plugin version : " + pluginVersion)
    }
}
