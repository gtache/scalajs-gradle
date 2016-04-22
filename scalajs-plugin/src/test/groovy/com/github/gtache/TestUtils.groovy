package com.github.gtache

import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.testfixtures.ProjectBuilder

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

        proj.extensions.add("offlineLib", proj.file("../gradleTestLibs"))
        proj.dependencies {
            if (((File) proj.property("offlineLib")).exists()) {
                println("exists")
                compile fileTree(offlineLib)
            } else {
                println("doesnt")
                compile 'org.scala-lang:scala-compiler:2.11.8'
                compile 'org.scala-lang:scala-library:2.11.8'
                compile group: 'org.scala-js', name: 'scalajs-sbt-test-adapter_2.11', version: '0.6.8'
                compile group: 'org.scala-js', name: 'scalajs-js-envs_2.11', version: '0.6.8'
                compile group: 'org.scala-js', name: 'scalajs-tools_2.11', version: '0.6.8'
                //    }
            }
            Copy libCopy = proj.tasks.create("copyToLib", Copy.class)
            libCopy.from(proj.configurations.compile.files)
            libCopy.into(((File) proj.property('offlineLib')))
            return proj
        }
    }

    public static void applyPlugin(Project project) {
        project.pluginManager.apply('scalajs-plugin')
    }

    public static void setProperty(Project project, String key, Object value = true) {
        project.extensions.add(key, value)
    }

}
