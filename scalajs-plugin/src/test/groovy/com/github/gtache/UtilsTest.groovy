package com.github.gtache

import com.github.gtache.testing.TestFramework
import org.gradle.api.Project
import org.junit.Test
import org.scalajs.core.tools.linker.backend.OutputMode
import org.scalajs.core.tools.logging.Level
import org.scalajs.jsenv.nodejs.NodeJSEnv
import org.scalajs.jsenv.phantomjs.PhantomJSEnv
import org.scalajs.jsenv.rhino.RhinoJSEnv
import scala.collection.mutable.ArrayBuffer

import static com.github.gtache.TestUtils.*
import static com.github.gtache.Utils.*
import static com.github.gtache.tasks.CompileJSTask.MIN_OUTPUT
import static com.github.gtache.tasks.CompileJSTask.OUTPUT

class UtilsTest extends GroovyTestCase {

    @Test
    public void testResolvePath() {
        Project project = getFreshProject()
        final dummyString = 'foo/bar.js'
        def jsDir = project.file(project.buildDir.absolutePath + JS_REL_DIR)
        def jsPath = project.file(jsDir.path + File.separator + project.name + EXT).path
        def jsFastPath = project.file(jsDir.path + File.separator + project.name + FASTOPT_SUFFIX).path
        def jsFullPath = project.file(jsDir.path + File.separator + project.name + FULLOPT_SUFFIX).path
        def dummyPath = project.file(dummyString).toString()
        applyPlugin(project)
        assertEquals(jsFastPath, resolvePath(project))
        deleteRecursive(project.projectDir)

        project = getFreshProject()
        jsDir = project.file(project.buildDir.absolutePath + JS_REL_DIR)
        jsPath = project.file(jsDir.path + File.separator + project.name + EXT).path
        jsFastPath = project.file(jsDir.path + File.separator + project.name + FASTOPT_SUFFIX).path
        jsFullPath = project.file(jsDir.path + File.separator + project.name + FULLOPT_SUFFIX).path
        setProperty(project, RUN_FULL)
        applyPlugin(project)
        assertEquals(jsFullPath, resolvePath(project))
        deleteRecursive(project.projectDir)

        project = getFreshProject()
        jsDir = project.file(project.buildDir.absolutePath + JS_REL_DIR)
        jsPath = project.file(jsDir.path + File.separator + project.name + EXT).path
        jsFastPath = project.file(jsDir.path + File.separator + project.name + FASTOPT_SUFFIX).path
        jsFullPath = project.file(jsDir.path + File.separator + project.name + FULLOPT_SUFFIX).path
        setProperty(project, RUN_NOOPT)
        applyPlugin(project)
        assertEquals(jsPath, resolvePath(project))
        deleteRecursive(project.projectDir)

        project = getFreshProject()
        jsDir = project.file(project.buildDir.absolutePath + JS_REL_DIR)
        jsPath = project.file(jsDir.path + File.separator + project.name + EXT).path
        jsFastPath = project.file(jsDir.path + File.separator + project.name + FASTOPT_SUFFIX).path
        jsFullPath = project.file(jsDir.path + File.separator + project.name + FULLOPT_SUFFIX).path
        setProperty(project, RUN_FULL)
        setProperty(project, RUN_NOOPT)
        applyPlugin(project)
        assertEquals(jsFullPath, resolvePath(project))
        deleteRecursive(project.projectDir)

        project = getFreshProject()
        dummyPath = project.file(dummyString).toString()
        setProperty(project, MIN_OUTPUT, dummyString)
        applyPlugin(project)
        assertEquals(dummyPath, resolvePath(project))
        deleteRecursive(project.projectDir)

        project = getFreshProject()
        dummyPath = project.file(dummyString).toString()
        setProperty(project, OUTPUT, dummyString)
        applyPlugin(project)
        assertEquals(dummyPath, resolvePath(project))
        deleteRecursive(project.projectDir)

        project = getFreshProject()
        dummyPath = project.file(dummyString).toString()
        setProperty(project, MIN_OUTPUT, dummyString)
        setProperty(project, OUTPUT, 'blabla/bla.js')
        applyPlugin(project)
        assertEquals(dummyPath, resolvePath(project))
        deleteRecursive(project.projectDir)

        project = getFreshProject()
        dummyPath = project.file(dummyString).toString()
        setProperty(project, MIN_OUTPUT, dummyString)
        setProperty(project, RUN_FULL)
        applyPlugin(project)
        assertEquals(dummyPath, resolvePath(project))
        deleteRecursive(project.projectDir)

        project = getFreshProject()
        dummyPath = project.file(dummyString).toString()
        setProperty(project, OUTPUT, dummyString)
        setProperty(project, RUN_FULL)
        applyPlugin(project)
        assertEquals(dummyPath, resolvePath(project))
        deleteRecursive(project.projectDir)

    }

    @Test
    public void testResolveEnv() {
        Project project = getFreshProject()
        applyPlugin(project)
        assertEquals(NodeJSEnv.class, resolveEnv(project).getClass())
        deleteRecursive(project.projectDir)

        project = getFreshProject()
        setProperty(project, RHINO)
        applyPlugin(project)
        assertEquals(RhinoJSEnv.class, resolveEnv(project).getClass())
        deleteRecursive(project.projectDir)

        project = getFreshProject()
        setProperty(project, PHANTOM)
        applyPlugin(project)
        assertEquals(PhantomJSEnv.class, resolveEnv(project).getClass())
        deleteRecursive(project.projectDir)

    }

    @Test
    public void testResolveLogLevel() {
        final String log = "logLevel"
        Project project = getFreshProject()
        applyPlugin(project)
        assertEquals(Level.Debug$.MODULE$, resolveLogLevel(project, log, Level.Debug$.MODULE$))
        assertEquals(Level.Info$.MODULE$, resolveLogLevel(project, log, Level.Info$.MODULE$))
        assertEquals(Level.Warn$.MODULE$, resolveLogLevel(project, log, Level.Warn$.MODULE$))
        assertEquals(Level.Error$.MODULE$, resolveLogLevel(project, log, Level.Error$.MODULE$))
        deleteRecursive(project.projectDir)

        project = getFreshProject()
        setProperty(project, log, "Warn")
        applyPlugin(project)
        assertEquals(Level.Warn$.MODULE$, resolveLogLevel(project, log, Level.Debug$.MODULE$))
        deleteRecursive(project.projectDir)

        project = getFreshProject()
        setProperty(project, log, "Error")
        assertEquals(Level.Error$.MODULE$, resolveLogLevel(project, log, Level.Debug$.MODULE$))
        deleteRecursive(project.projectDir)

        project = getFreshProject()
        setProperty(project, log, "Debug")
        assertEquals(Level.Debug$.MODULE$, resolveLogLevel(project, log, Level.Warn$.MODULE$))
        deleteRecursive(project.projectDir)

        project = getFreshProject()
        setProperty(project, log, "Info")
        assertEquals(Level.Info$.MODULE$, resolveLogLevel(project, log, Level.Debug$.MODULE$))
        deleteRecursive(project.projectDir)
    }

    @Test
    public void testGetOutputMode() {
        assertEquals(OutputMode.ECMAScript51Global$.MODULE$, getOutputMode(ECMA_51_GLOBAL))
        assertEquals(OutputMode.ECMAScript51Isolated$.MODULE$, getOutputMode(ECMA_51_ISOLATED))
        assertEquals(OutputMode.ECMAScript6$.MODULE$, getOutputMode(ECMA_6))
    }

    @Test
    public void testGetMinimalDependencySeq() {
        final Project project = getFreshProject()
        final jsFastPath = project.name + FASTOPT_SUFFIX
        applyPlugin(project)
        final seq = getMinimalDependencySeq(project)
        assertEquals(1, seq.size())
        assertEquals(jsFastPath, seq.apply(0).lib().name())
        deleteRecursive(project.projectDir)
    }

    @Test
    public void testDeleteRecursive() {
        final Project project = getFreshProject()
        final File root = project.file('test')
        root.mkdir()
        final File file1 = project.file('test/1')
        final File file2 = project.file('test/2')
        final File dir1 = project.file('test/dir1')
        final File dir2 = project.file('test/dir2')
        dir1.mkdir()
        dir2.mkdir()
        final File file3 = project.file('test/dir1/1')
        final File file4 = project.file('test/dir1/2')
        final Set<File> allFiles = [root, dir1, dir2, file1, file2, file3, file4].toSet()
        allFiles.each {
            if (it.isDirectory()) {
                assertTrue(it.exists())
            } else {
                assertFalse(it.exists())
            }
        }
        file1.createNewFile()
        file2.createNewFile()
        file3.createNewFile()
        file4.createNewFile()
        assertEquals(4, root.listFiles().size())
        assertEquals(2, dir1.listFiles().size())
        assertEquals(0, dir2.listFiles().size())
        allFiles.each {
            assertTrue(it.exists())
        }
        deleteRecursive(root)
        allFiles.each {
            assertFalse(it.exists())
        }
        deleteRecursive(project.projectDir)
    }

    @Test
    public void testTaskInStartParameter() {
        final Project project = getFreshProject()
        final List<String> tasks = new ArrayList<>()
        tasks.add("TeSTJs")
        tasks.add("blaJS")
        project.gradle.startParameter.setTaskNames(tasks)
        assertTrue(isTaskInStartParameter(project, "testjs"))
        assertTrue(isTaskInStartParameter(project, "blaJS"))
        assertFalse(isTaskInStartParameter(project, "something"))
        deleteRecursive(project.projectDir)
    }

    @Test
    public void testResolveTestFrameworks() {
        Project project = getFreshProject()
        assertTrue(resolveTestFrameworks(project).isEmpty())
        deleteRecursive(project.projectDir)
        project = getFreshProject()
        final List<String> testFrameworks = new ArrayList<>()
        final String test1 = "com.test.Test1"
        final String test2 = "com.test.Test2"
        testFrameworks.add(test1)
        testFrameworks.add(test2)
        project.extensions.add(TEST_FRAMEWORKS, testFrameworks)
        final List<TestFramework> resolvedFrameworks = resolveTestFrameworks(project)
        assertEquals(2, resolvedFrameworks.size())
        final List<ArrayBuffer<String>> resolvedFrameworksName = resolvedFrameworks.collect {
            it.classNames()
        }
        boolean found1 = false
        boolean found2 = false
        resolvedFrameworksName.each {
            if (it.contains(test1)) {
                found1 = true
            }
            if (it.contains(test2)) {
                found2 = true
            }
        }
        assertTrue(found1 && found2)
        deleteRecursive(project.projectDir)
    }

    @Test
    public void testToRegex() {
        assertEquals("com\\..*", toRegex("com.*"))
        assertEquals("com\\.github\\.gtache", toRegex("com.github.gtache"))
        assertEquals(".*", toRegex("*"))
        assertEquals("com\\\\gtache", toRegex("com\\gtache"))
    }
}
