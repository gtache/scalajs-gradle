package com.github.gtache

import org.gradle.api.Project
import org.junit.Test


class UtilsTest extends GroovyTestCase {

    @Test
    public void testResolvePath() {
        Project project = PluginTest.freshProject
        final def jsDir = project.file('js/')
        final def jsPath = project.file(jsDir.path + '/' + project.name + '.js').path
        final def jsFastPath = project.file(jsDir.path + '/' + project.name + '_fastopt.js').path
        final def jsFullPath = project.file(jsDir.path + '/' + project.name + '_fullopt.js').path
        final def dummyString = 'foo/bar.js'
        final def dummyPath = project.file(dummyString).toString()
        project.plugins.apply('scalajs-plugin')
        assertEquals(Utils.resolvePath(project),jsFastPath)
        project= PluginTest.freshProject
        project.setProperty('runFull',null)
        project.plugins.apply('scalajs-plugin')
        assertEquals(Utils.resolvePath(project),jsFullPath)
        project = PluginTest.freshProject
        project.setProperty('runNoOpt',null)
        project.plugins.apply('scalajs-plugin')
        assertEquals(Utils.resolvePath(project),jsPath)
        project = PluginTest.freshProject
        project.setProperty('runFull',null)
        project.setProperty('runNoOpt',null)
        project.plugins.apply('scalajs-plugin')
        assertEquals(Utils.resolvePath(project),jsFullPath)
        project = PluginTest.freshProject
        project.setProperty('o',dummyString)
        project.plugins.apply('scalajs-plugin')
        assertEquals(Utils.resolvePath(project),dummyPath)
        project = PluginTest.freshProject
        project.setProperty('output',dummyString)
        project.plugins.apply('scalajs-plugin')
        assertEquals(Utils.resolvePath(project),dummyPath)
        project=PluginTest.freshProject
        project.setProperty('o',dummyString)
        project.setProperty('output',"blabla/bla.js")
        project.plugins.apply('scalajs-plugin')
        assertEquals(Utils.resolvePath(project),dummyPath)
        project = PluginTest.freshProject
        project.setProperty('o',dummyString)
        project.setProperty('runFull',null)
        project.plugins.apply('scalajs-plugin')
        assertEquals(Utils.resolvePath(project),dummyPath)
        project = PluginTest.freshProject
        project.setProperty('output',dummyString)
        project.setProperty('runFull',null)
        project.plugins.apply('scalajs-plugin')
        assertEquals(Utils.resolvePath(project),dummyPath)

    }

    @Test
    public void testResolveEnv() {

    }

    @Test
    public void testResolveLogLevel() {

    }

    @Test
    public void testGetOutputMode() {

    }

    @Test
    public void testGetMinimalDependencySeq() {

    }
}
