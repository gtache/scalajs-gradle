package com.github.gtache.tasks

import com.github.gtache.testing.GradleRunner
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import sbt.testing.TaskDef


class TestJSTask extends DefaultTask {

    @TaskAction
    def run(){
        GradleRunner runner = new GradleRunner()
        runner.tasks(new TaskDef[0])
    }
}
