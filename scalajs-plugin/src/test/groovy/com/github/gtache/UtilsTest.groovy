package com.github.gtache

import org.gradle.api.Project
import org.junit.Test
import org.scalajs.core.tools.linker.backend.OutputMode
import org.scalajs.core.tools.logging.Level
import org.scalajs.jsenv.nodejs.NodeJSEnv
import org.scalajs.jsenv.phantomjs.PhantomJSEnv
import org.scalajs.jsenv.rhino.RhinoJSEnv

class UtilsTest extends GroovyTestCase {

    @Test
    public void testResolvePath() {
        Project project = TestUtils.getFreshProject()
        final def dummyString = 'foo/bar.js'
        def jsDir = project.file('js/')
        def jsPath = project.file(jsDir.path + '/' + project.name + '.js').path
        def jsFastPath = project.file(jsDir.path + '/' + project.name + '_fastopt.js').path
        def jsFullPath = project.file(jsDir.path + '/' + project.name + '_fullopt.js').path
        def dummyPath = project.file(dummyString).toString()
        TestUtils.applyPlugin(project)
        assertEquals(jsFastPath, Utils.resolvePath(project))
        Utils.deleteRecursive(project.projectDir)

        project = TestUtils.getFreshProject()
        jsDir = project.file('js/')
        jsFullPath = project.file(jsDir.path + '/' + project.name + '_fullopt.js').path
        TestUtils.setProperty(project, 'runFull')
        TestUtils.applyPlugin(project)
        assertEquals(jsFullPath, Utils.resolvePath(project))
        Utils.deleteRecursive(project.projectDir)

        project = TestUtils.getFreshProject()
        jsDir = project.file('js/')
        jsPath = project.file(jsDir.path + '/' + project.name + '.js').path
        TestUtils.setProperty(project, 'runNoOpt')
        TestUtils.applyPlugin(project)
        assertEquals(jsPath, Utils.resolvePath(project))
        Utils.deleteRecursive(project.projectDir)

        project = TestUtils.getFreshProject()
        jsDir = project.file('js/')
        jsFullPath = project.file(jsDir.path + '/' + project.name + '_fullopt.js').path
        TestUtils.setProperty(project, 'runFull')
        TestUtils.setProperty(project, 'runNoOpt')
        TestUtils.applyPlugin(project)
        assertEquals(jsFullPath, Utils.resolvePath(project))
        Utils.deleteRecursive(project.projectDir)

        project = TestUtils.getFreshProject()
        dummyPath = project.file(dummyString).toString()
        TestUtils.setProperty(project, 'o', dummyString)
        TestUtils.applyPlugin(project)
        assertEquals(dummyPath, Utils.resolvePath(project))
        Utils.deleteRecursive(project.projectDir)

        project = TestUtils.getFreshProject()
        dummyPath = project.file(dummyString).toString()
        TestUtils.setProperty(project, 'output', dummyString)
        TestUtils.applyPlugin(project)
        assertEquals(dummyPath, Utils.resolvePath(project))
        Utils.deleteRecursive(project.projectDir)

        project = TestUtils.getFreshProject()
        dummyPath = project.file(dummyString).toString()
        TestUtils.setProperty(project, 'o', dummyString)
        TestUtils.setProperty(project, 'output', 'blabla/bla.js')
        TestUtils.applyPlugin(project)
        assertEquals(dummyPath, Utils.resolvePath(project))
        Utils.deleteRecursive(project.projectDir)

        project = TestUtils.getFreshProject()
        dummyPath = project.file(dummyString).toString()
        TestUtils.setProperty(project, 'o', dummyString)
        TestUtils.setProperty(project, 'runFull')
        TestUtils.applyPlugin(project)
        assertEquals(dummyPath, Utils.resolvePath(project))
        Utils.deleteRecursive(project.projectDir)

        project = TestUtils.getFreshProject()
        dummyPath = project.file(dummyString).toString()
        TestUtils.setProperty(project, 'output', dummyString)
        TestUtils.setProperty(project, 'runFull')
        TestUtils.applyPlugin(project)
        assertEquals(dummyPath, Utils.resolvePath(project))
        Utils.deleteRecursive(project.projectDir)

    }

    @Test
    public void testResolveEnv() {
        final String rhino = "rhino"
        final String phantom = "phantom"
        Project project = TestUtils.getFreshProject()
        TestUtils.applyPlugin(project)
        assertEquals(NodeJSEnv.class, Utils.resolveEnv(project).getClass())
        Utils.deleteRecursive(project.projectDir)

        project = TestUtils.getFreshProject()
        TestUtils.setProperty(project, rhino)
        TestUtils.applyPlugin(project)
        assertEquals(RhinoJSEnv.class, Utils.resolveEnv(project).getClass())
        Utils.deleteRecursive(project.projectDir)

        project = TestUtils.getFreshProject()
        TestUtils.setProperty(project, phantom)
        TestUtils.applyPlugin(project)
        assertEquals(PhantomJSEnv.class, Utils.resolveEnv(project).getClass())
        Utils.deleteRecursive(project.projectDir)

    }

    @Test
    public void testResolveLogLevel() {
        final String log = "logLevel"
        Project project = TestUtils.getFreshProject()
        TestUtils.applyPlugin(project)
        assertEquals(Level.Debug$.MODULE$, Utils.resolveLogLevel(project, log, Level.Debug$.MODULE$))
        assertEquals(Level.Info$.MODULE$, Utils.resolveLogLevel(project, log, Level.Info$.MODULE$))
        assertEquals(Level.Warn$.MODULE$, Utils.resolveLogLevel(project, log, Level.Warn$.MODULE$))
        assertEquals(Level.Error$.MODULE$, Utils.resolveLogLevel(project, log, Level.Error$.MODULE$))
        Utils.deleteRecursive(project.projectDir)

        project = TestUtils.getFreshProject()
        TestUtils.setProperty(project, log, "Warn")
        TestUtils.applyPlugin(project)
        assertEquals(Level.Warn$.MODULE$, Utils.resolveLogLevel(project, log, Level.Debug$.MODULE$))
        Utils.deleteRecursive(project.projectDir)

        project = TestUtils.getFreshProject()
        TestUtils.setProperty(project, log, "Error")
        assertEquals(Level.Error$.MODULE$, Utils.resolveLogLevel(project, log, Level.Debug$.MODULE$))
        Utils.deleteRecursive(project.projectDir)

        project = TestUtils.getFreshProject()
        TestUtils.setProperty(project, log, "Debug")
        assertEquals(Level.Debug$.MODULE$, Utils.resolveLogLevel(project, log, Level.Warn$.MODULE$))
        Utils.deleteRecursive(project.projectDir)

        project = TestUtils.getFreshProject()
        TestUtils.setProperty(project, log, "Info")
        assertEquals(Level.Info$.MODULE$, Utils.resolveLogLevel(project, log, Level.Debug$.MODULE$))
        Utils.deleteRecursive(project.projectDir)
    }

    @Test
    public void testGetOutputMode() {
        final String one = "ECMAScript51Global"
        final String two = "ECMAScript51Isolated"
        final String three = "ECMAScript6"
        assertEquals(OutputMode.ECMAScript51Global$.MODULE$, Utils.getOutputMode(one))
        assertEquals(OutputMode.ECMAScript51Isolated$.MODULE$, Utils.getOutputMode(two))
        assertEquals(OutputMode.ECMAScript6$.MODULE$, Utils.getOutputMode(three))

    }

    @Test
    public void testGetMinimalDependencySeq() {
        final Project project = TestUtils.getFreshProject()
        final def jsFastPath = project.name + '_fastopt.js'
        TestUtils.applyPlugin(project)
        final def seq = Utils.getMinimalDependencySeq(project)
        assertEquals(1, seq.size())
        assertEquals(jsFastPath, seq.apply(0).lib().name())
        Utils.deleteRecursive(project.projectDir)
    }
}
