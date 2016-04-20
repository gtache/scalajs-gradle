package com.github.gtache

import com.github.gtache.tasks.CompileJSTask
import com.google.common.collect.Sets
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.junit.Test
import org.scalajs.core.tools.logging.Level

class PluginTest extends GroovyTestCase {
    Project project

    @Test
    public void testAllConfigurations() {
        def optionsList = [
                "o=js2/js.js",
                "output=js2/js2.js",
                "p",
                "prettyPrint",
                "s",
                "sourceMap",
                "compliantAsInstanceOfs",
                "m",
                "outputMode=ECMAScript6",
                "c",
                "checkIR",
                //"r",
                //"relativizeSourceMap",
                "linkLogLevel=Debug",
                "d",
                "q",
                "qq"
        ]
        def ret = Sets.powerSet(optionsList.toSet())
        ret.each {
            checkProperties(it)
        }
    }

    private void checkProperties(Set<String> p) {
        project = TestUtils.getFreshProject()
        TestUtils.addDummyScalaJSFile(project)
        p.each {
            if (it.contains('=')) {
                def res = it.split("=")
                TestUtils.setProperty(project, res[0], res[1])
            } else {
                TestUtils.setProperty(project, it)
            }
        }
        TestUtils.applyPlugin(project)
        ((CompileJSTask) project.tasks.getByName("FastOptJS")).run()
        p.each {
            checkProperty(it, p)
        }
    }

    private void checkProperty(String s, Set<String> p) {
        def options = Scalajsld$.MODULE$.options()
        switch (s) {
            case 'o':
                assertEquals(options.output().path, project.file(project.property(s)).path)
                break
            case 'output':
                if (!p.contains('o')) {
                    assertEquals(options.output().path, project.file(project.property(s)).path)
                }
                break
            case 'p':
            case 'prettyPrint':
                assertTrue(options.prettyPrint())
                break
            case 's':
            case 'sourceMap':
                assertTrue(options.sourceMap())
                break
            case 'compliantAsInstanceOfs':
                //TODO
                break
            case 'm':
                assertEquals(options.outputMode(), Utils.getOutputMode((String) project.property(s)))
                break
            case 'outputMode':
                if (!p.contains('m')) {
                    assertEquals(options.outputMode(), Utils.getOutputMode((String) project.property(s)))
                }
                break
            case 'c':
            case 'checkIR':
                assertTrue(options.checkIR())
                break
            case 'r':
                //TODO
                break
            case 'relativizeSourceMap':
                //TODO
                break
            case 'linkLogLevel':
                if (!(p.contains('q') || p.contains('qq') || p.contains('d') ||
                        p.contains('quiet') || p.contains('really-quiet') || p.contains('debug'))) {
                    assertEquals(Utils.resolveLogLevel(project, (String) project.property('linkLogLevel'), Level.Info$.MODULE$),
                            options.logLevel())
                }
                break
            case 'd':
            case 'debug':
                assertEquals(Level.Debug$.MODULE$, options.logLevel())
                break
            case 'q':
            case 'quiet':
                if (!(p.contains('d') || p.contains('debug'))) {
                    assertEquals(Level.Warn$.MODULE$, options.logLevel())
                }
                break
            case 'qq':
            case 'really-quiet':
                if (!(p.contains('d') || p.contains('debug') ||
                        p.contains('q') || p.contains('quiet'))) {
                    assertEquals(Level.Error$.MODULE$, options.logLevel())
                }
                break
            default:
                break
        }
    }

    @Test
    public void testTasksPluginsDependenciesAdded() {
        project = TestUtils.getFreshProject()
        TestUtils.applyPlugin(project)
        def allTasks = [
                "TestJS",
                "FastOptJS",
                "FullOptJS",
                "RunJS",
                "NoOptJS",
                "CleanAll"
        ]
        allTasks.each {
            assertTrue(project.tasks.findByPath(it) != null)
        }

        def plugins = [
                "java",
                "scala",
                "scalajs-plugin"
        ]

        plugins.each {
            assertTrue(project.plugins.findPlugin(it) != null)
        }

        def libDep = new DefaultExternalModuleDependency('org.scala-js', 'scalajs-library_2.11', '0.6.8')
        def compDep = new DefaultExternalModuleDependency('org.scala-js', 'scalajs-compiler_2.11.8', '0.6.8')
        def compileIt = project.configurations.getByName('compile').dependencies.iterator()
        def libDepFound = false
        while (compileIt.hasNext() && !libDepFound) {
            def dep = compileIt.next()
            if (libDep.group == dep.group && libDep.name == dep.name && libDep.version == dep.version) {
                libDepFound = true
            }
        }
        def scalaCompileIt = project.configurations.getByName('scalaCompilePlugin').dependencies.iterator()
        def compileDepFound = false
        while (scalaCompileIt.hasNext() && !compileDepFound) {
            def dep = scalaCompileIt.next()
            if (compDep.group == dep.group && compDep.name == dep.name && compDep.version == dep.version) {
                compileDepFound = true
            }
        }
        assertTrue(libDepFound)
        assertTrue(compileDepFound)
    }

}
