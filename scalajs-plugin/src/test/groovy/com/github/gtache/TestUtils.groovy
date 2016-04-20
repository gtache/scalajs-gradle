package com.github.gtache

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import java.util.concurrent.locks.Lock


class TestUtils {

    public static Project getFreshProject() {

        Project proj = ProjectBuilder.builder().build()

        proj.buildscript {
            repositories {
                mavenLocal()
                mavenCentral()
            }
            dependencies {
                classpath 'com.github.gtache:scalajs-plugin:0.1.2'
            }
        }
        proj.repositories {
            mavenCentral()
        }

        proj.pluginManager.apply('java')

        proj.pluginManager.apply('scala')

        proj.dependencies {
            compile 'org.scala-lang:scala-compiler:2.11.8'
            compile 'org.scala-lang:scala-library:2.11.8'
            compile group: 'org.scala-js', name: 'scalajs-sbt-test-adapter_2.11', version: '0.6.8'
            compile group: 'org.scala-js', name: 'scalajs-js-envs_2.11', version: '0.6.8'
            compile group: 'org.scala-js', name: 'scalajs-tools_2.11', version: '0.6.8'
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
