package com.github.gtache

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import static com.github.gtache.BuildConfig.PLUGIN_VERSION
import static com.github.gtache.BuildConfig.SCALA_FULL_VERSION

class TestUtils {

    public static Project getFreshProject() {

        Project proj = ProjectBuilder.builder().build()

        proj.buildscript {
            repositories {
                mavenLocal()
                mavenCentral()
            }
            dependencies {
                classpath 'com.github.gtache:scalajs-plugin:' + PLUGIN_VERSION
            }
        }
        proj.repositories {
            mavenCentral()
        }

        proj.pluginManager.apply('java')

        proj.pluginManager.apply('scala')

        proj.dependencies {
            compile 'org.scala-lang:scala-compiler:' + SCALA_FULL_VERSION
            compile 'org.scala-lang:scala-library:' + SCALA_FULL_VERSION
        }
        return proj
    }

    public static void applyPlugin(Project project) {
        project.pluginManager.apply('scalajs-plugin')
    }

    public static void setProperty(Project project, String key, Object value = true) {
        project.extensions.add(key, value)
    }

}
